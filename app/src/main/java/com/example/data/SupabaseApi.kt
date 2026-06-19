package com.example.data

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.*

data class SupabaseRecord(
    val id: String,
    val reportData: String,
    val timestamp: String,
    val village_name: String,
    val entry_date: String,
    val entry_time: String
)

interface SupabaseService {
    @GET("reportsStore")
    suspend fun getRecords(
        @Query("select") select: String = "*"
    ): List<SupabaseRecord>

    @POST("reportsStore")
    suspend fun insertRecord(
        @Body record: SupabaseRecord,
        @Header("Prefer") prefer: String = "return=minimal"
    ): Response<Unit>

    @DELETE("reportsStore")
    suspend fun deleteRecord(
        @Query("id") idFilter: String // e.g., "eq.<UUID>"
    ): Response<Unit>
}

object SupabaseApi {
    private const val BASE_URL = "https://rsjyvrnxqkjtaczwectr.supabase.co/rest/v1/"
    private const val ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InJzanl2cm54cWtqdGFjendlY3RyIiwicm9sZSI6ImFub24iLCJpYXQiOjE3ODEzMDMwNjUsImV4cCI6MjA5Njg3OTA2NX0.YB63IkqX1Q5uFtNNxEjnyx-TFdCeN_U09vWM6zguL-o"

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val request = chain.request().newBuilder()
                .header("apikey", ANON_KEY)
                .header("Authorization", "Bearer $ANON_KEY")
                .header("Content-Type", "application/json")
                .build()
            chain.proceed(request)
        }
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.HEADERS
        })
        .build()

    val service: SupabaseService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(SupabaseService::class.java)
    }
}
