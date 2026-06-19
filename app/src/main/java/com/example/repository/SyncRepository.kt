package com.example.repository

import com.example.data.*
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.*

data class LocalComplaintDetails(
    val title: String,
    val type: String,
    val description: String,
    val submitterEmail: String,
    val submitterName: String,
    val submitterPhone: String,
    val isSmsSubscribed: Boolean,
    val status: String,
    val latitude: Double,
    val longitude: Double
)

object SerializationHelper {
    private val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
    private val adapter = moshi.adapter(LocalComplaintDetails::class.java)

    fun toJson(details: LocalComplaintDetails): String {
        return adapter.toJson(details)
    }

    fun fromJson(json: String): LocalComplaintDetails? {
        return try {
            adapter.fromJson(json)
        } catch (e: Exception) {
            null
        }
    }
}

fun String.toSha256(): String {
    val bytes = MessageDigest.getInstance("SHA-256").digest(this.toByteArray())
    return bytes.joinToString("") { "%02x".format(it) }
}

class SyncRepository(
    private val userDao: UserDao,
    private val complaintDao: ComplaintDao,
    private val supabaseService: SupabaseService = SupabaseApi.service
) {
    // Authentication
    suspend fun login(email: String, passwordRaw: String): UserEntity? = withContext(Dispatchers.IO) {
        val user = userDao.getUserByEmail(email) ?: return@withContext null
        val passHash = passwordRaw.toSha256()
        if (user.passwordHash == passHash) {
            user
        } else {
            null
        }
    }

    suspend fun register(email: String, passwordRaw: String, name: String): Boolean = withContext(Dispatchers.IO) {
        val existing = userDao.getUserByEmail(email)
        if (existing != null) return@withContext false

        val passHash = passwordRaw.toSha256()
        userDao.registerUser(UserEntity(email = email, passwordHash = passHash, name = name))
        true
    }

    // Live Flow updates from Local DB Cache
    fun getLocalComplaints(userEmail: String): Flow<List<ComplaintEntity>> {
        return complaintDao.getComplaintsByUser(userEmail)
    }

    fun getAllLocalComplaints(): Flow<List<ComplaintEntity>> {
        return complaintDao.getAllComplaints()
    }

    // Submit a complaint
    suspend fun submitComplaint(
        title: String,
        type: String,
        description: String,
        email: String,
        userName: String,
        phone: String,
        isSmsSubscribed: Boolean,
        villageName: String,
        latitude: Double,
        longitude: Double
    ): ComplaintEntity = withContext(Dispatchers.IO) {
        val id = UUID.randomUUID().toString()
        val calendar = Calendar.getInstance()
        val entryDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(calendar.time)
        val entryTime = SimpleDateFormat("hh:mm:ss a", Locale.getDefault()).format(calendar.time)
        val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        val timestampIso = isoFormat.format(calendar.time)

        val complaint = ComplaintEntity(
            id = id,
            title = title,
            type = type,
            description = description,
            submitterEmail = email,
            submitterName = userName,
            submitterPhone = phone,
            isSmsSubscribed = isSmsSubscribed,
            status = "Pending",
            villageName = villageName,
            entryDate = entryDate,
            entryTime = entryTime,
            timestamp = calendar.timeInMillis,
            latitude = latitude,
            longitude = longitude,
            isSynced = false
        )

        complaintDao.insertComplaint(complaint)
        complaint
    }

    // Sync all offline rows to cloud
    suspend fun syncAllWithCloud(): Int = withContext(Dispatchers.IO) {
        val unsynced = complaintDao.getUnsyncedComplaints()
        var successCount = 0
        for (item in unsynced) {
            val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }
            val timestampIso = isoFormat.format(Date(item.timestamp))

            // Map standard layout content to reportData
            val details = LocalComplaintDetails(
                title = item.title,
                type = item.type,
                description = item.description,
                submitterEmail = item.submitterEmail,
                submitterName = item.submitterName,
                submitterPhone = item.submitterPhone,
                isSmsSubscribed = item.isSmsSubscribed,
                status = item.status,
                latitude = item.latitude,
                longitude = item.longitude
            )
            val jsonPayload = SerializationHelper.toJson(details)

            val apiRecord = SupabaseRecord(
                id = item.id,
                reportData = jsonPayload,
                timestamp = timestampIso,
                village_name = item.villageName,
                entry_date = item.entryDate,
                entry_time = item.entryTime
            )

            try {
                val response = supabaseService.insertRecord(apiRecord)
                if (response.isSuccessful) {
                    complaintDao.markAsSynced(item.id)
                    successCount++
                }
            } catch (e: Exception) {
                // Network failure or timeout, retain offline status
                e.printStackTrace()
            }
        }
        successCount
    }

    // Fetch cloud ledger list and parse them
    suspend fun fetchCloudLedger(): List<ComplaintEntity> = withContext(Dispatchers.IO) {
        try {
            val records = supabaseService.getRecords()
            val parsedList = records.map { rec ->
                val details = SerializationHelper.fromJson(rec.reportData)
                val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
                    timeZone = TimeZone.getTimeZone("UTC")
                }
                val parsedDateObj = try { isoFormat.parse(rec.timestamp) } catch (e: Exception) { Date() }
                val timeMillis = parsedDateObj?.time ?: System.currentTimeMillis()

                ComplaintEntity(
                    id = rec.id,
                    title = details?.title ?: "Infield System Record",
                    type = details?.type ?: "General",
                    description = details?.description ?: rec.reportData,
                    submitterEmail = details?.submitterEmail ?: "",
                    submitterName = details?.submitterName ?: "Panchayat Infield Officer",
                    submitterPhone = details?.submitterPhone ?: "",
                    isSmsSubscribed = details?.isSmsSubscribed ?: false,
                    status = details?.status ?: "Pending",
                    villageName = rec.village_name,
                    entryDate = rec.entry_date,
                    entryTime = rec.entry_time,
                    timestamp = timeMillis,
                    latitude = details?.latitude ?: 0.0,
                    longitude = details?.longitude ?: 0.0,
                    isSynced = true
                )
            }
            parsedList
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    // Delete single item from local + cloud
    suspend fun deleteComplaint(id: String, isOnline: Boolean): Boolean = withContext(Dispatchers.IO) {
        var success = true
        if (isOnline) {
            try {
                val res = supabaseService.deleteRecord("eq.$id")
                success = res.isSuccessful
            } catch (e: Exception) {
                e.printStackTrace()
                success = false
            }
        }
        if (success || !isOnline) {
            complaintDao.deleteComplaintById(id)
        }
        success
    }
}
