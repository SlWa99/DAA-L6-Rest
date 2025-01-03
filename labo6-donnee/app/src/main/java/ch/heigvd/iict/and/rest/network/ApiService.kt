package ch.heigvd.iict.and.rest.network

import ch.heigvd.iict.and.rest.models.Contact
import okhttp3.ResponseBody
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface ApiService {
    @GET("enroll")
    suspend fun enroll(): ResponseBody

    @GET("contacts")
    suspend fun getContacts(@Header("X-UUID") uuid: String): List<Contact>

    @POST("contacts")
    suspend fun createContact(
        @Header("X-UUID") uuid: String,
        @Body contact: Contact
    ): Contact

    @PUT("contacts/{id}")
    suspend fun updateContact(
        @Header("X-UUID") uuid: String,
        @Path("id") contactId: Long,
        @Body contact: Contact
    ): Contact

    @DELETE("contacts/{id}")
    suspend fun deleteContact(
        @Header("X-UUID") uuid: String,
        @Path("id") contactId: Long
    )
}