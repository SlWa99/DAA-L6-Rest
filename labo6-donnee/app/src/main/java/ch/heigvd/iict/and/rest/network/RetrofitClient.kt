package ch.heigvd.iict.and.rest.network

import ch.heigvd.iict.and.rest.database.converters.CalendarJsonAdapter
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.util.Calendar

object RetrofitClient {
    private const val BASE_URL = "https://daa.iict.ch/"

    private val gson = GsonBuilder()
        .setLenient()
        .registerTypeAdapter(Calendar::class.java, CalendarJsonAdapter())
        .create()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(ScalarsConverterFactory.create()) // Pour l'endpoint enroll
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()

    val apiService: ApiService = retrofit.create(ApiService::class.java)
}