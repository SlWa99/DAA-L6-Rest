package ch.heigvd.iict.and.rest.network

import ch.heigvd.iict.and.rest.models.Contact
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header

interface ApiService {
    @GET("enroll")
    suspend fun enroll(): String

    @GET("contacts")
    suspend fun getContacts(@Header("X-UUID") uuid: String): List<Contact>
}