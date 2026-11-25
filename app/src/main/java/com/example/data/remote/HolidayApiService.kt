package com.example.data.remote

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Url
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = true)
data class HolidayCnResponse(
    @Json(name = "year") val year: Int? = null,
    @Json(name = "days") val days: List<HolidayCnDay> = emptyList()
)

@JsonClass(generateAdapter = true)
data class HolidayCnDay(
    @Json(name = "name") val name: String,
    @Json(name = "date") val date: String,
    @Json(name = "isOffDay") val isOffDay: Boolean
)

@JsonClass(generateAdapter = true)
data class TimorHolidayResponse(
    @Json(name = "code") val code: Int = -1,
    @Json(name = "holiday") val holiday: Map<String, TimorHolidayDay>? = null
)

@JsonClass(generateAdapter = true)
data class TimorHolidayDay(
    @Json(name = "holiday") val holiday: Boolean = false,
    @Json(name = "name") val name: String = "",
    @Json(name = "date") val date: String = ""
)

interface HolidayApiService {
    @GET
    suspend fun fetchHolidayCn(@Url url: String): HolidayCnResponse

    @GET
    suspend fun fetchTimor(@Url url: String): TimorHolidayResponse
}

object HolidayNetworkClient {
    val moshi: Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(12, TimeUnit.SECONDS)
        .addInterceptor { chain ->
            val request = chain.request().newBuilder()
                .header("User-Agent", "HolidayAlarm-OneUI9/1.0 (Android)")
                .header("Accept", "application/json")
                .build()
            chain.proceed(request)
        }
        .build()

    val apiService: HolidayApiService = Retrofit.Builder()
        .baseUrl("https://fastly.jsdelivr.net/")
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()
        .create(HolidayApiService::class.java)

    fun jsDelivrUrl(year: Int): String =
        "https://fastly.jsdelivr.net/gh/NateScarlet/holiday-cn@master/$year.json"

    fun githubRawUrl(year: Int): String =
        "https://raw.githubusercontent.com/NateScarlet/holiday-cn/master/$year.json"

    fun timorUrl(year: Int): String =
        "https://timor.tech/api/holiday/year/$year/"
}
