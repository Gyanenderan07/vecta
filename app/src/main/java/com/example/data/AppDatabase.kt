package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "users", indices = [Index(value = ["email"], unique = true)])
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val email: String,
    val passwordHash: String,
    val name: String
)

@Entity(tableName = "complaints")
data class ComplaintEntity(
    @PrimaryKey val id: String, // UUID string
    val title: String,
    val type: String,
    val description: String,
    val submitterEmail: String,
    val submitterName: String,
    val submitterPhone: String,
    val isSmsSubscribed: Boolean,
    val status: String, // "Pending", "Under Review", "Resolved"
    val villageName: String,
    val entryDate: String,
    val entryTime: String,
    val timestamp: Long,
    val latitude: Double,
    val longitude: Double,
    val isSynced: Boolean = false
)

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): UserEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun registerUser(user: UserEntity): Long
}

@Dao
interface ComplaintDao {
    @Query("SELECT * FROM complaints ORDER BY timestamp DESC")
    fun getAllComplaints(): Flow<List<ComplaintEntity>>

    @Query("SELECT * FROM complaints WHERE submitterEmail = :email ORDER BY timestamp DESC")
    fun getComplaintsByUser(email: String): Flow<List<ComplaintEntity>>

    @Query("SELECT * FROM complaints WHERE isSynced = 0")
    suspend fun getUnsyncedComplaints(): List<ComplaintEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComplaint(complaint: ComplaintEntity)

    @Query("UPDATE complaints SET isSynced = 1 WHERE id = :id")
    suspend fun markAsSynced(id: String)

    @Query("DELETE FROM complaints WHERE id = :id")
    suspend fun deleteComplaintById(id: String)

    @Query("DELETE FROM complaints")
    suspend fun clearAll()
}

@Database(entities = [UserEntity::class, ComplaintEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun complaintDao(): ComplaintDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: android.content.Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "ruralsync_database"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
