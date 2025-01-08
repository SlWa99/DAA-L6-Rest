/**
 * Nom du fichier : RetrofitClient.kt
 * Description    : Initialise le client Retrofit utilisé pour effectuer les appels réseau vers l'API.
 *                  Configure les adaptateurs JSON et les convertisseurs nécessaires.
 * Auteur         : ICI
 * Date           : 08 janvier 2025
 */
package ch.heigvd.iict.and.rest.network

import ch.heigvd.iict.and.rest.database.converters.CalendarJsonAdapter
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.util.Calendar

/**
 * Objet : RetrofitClient
 * Description : Fournit un singleton pour configurer et utiliser Retrofit avec des convertisseurs
 *               Gson et Scalars, adapté aux endpoints de l'API REST de l'application.
 */
object RetrofitClient {
    /**
     * Constante : BASE_URL
     * Description : URL de base pour accéder à l'API REST.
     */
    private const val BASE_URL = "https://daa.iict.ch/"

    /**
     * Instance : gson
     * Description : Utilise `GsonBuilder` pour configurer un convertisseur JSON qui gère les
     *               objets `Calendar` grâce à un adaptateur personnalisé.
     */
    private val gson = GsonBuilder()
        .setLenient()
        .registerTypeAdapter(Calendar::class.java, CalendarJsonAdapter())
        .create()

    /**
     * Instance : retrofit
     * Description : Initialise Retrofit en ajoutant des convertisseurs pour traiter les types
     *               JSON et chaînes de caractères, adaptés aux différents endpoints de l'API.
     */
    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(ScalarsConverterFactory.create()) // Pour l'endpoint enroll
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()

    /**
     * Instance : apiService
     * Description : Fournit une implémentation de l'interface `ApiService` configurée avec Retrofit.
     */
    val apiService: ApiService = retrofit.create(ApiService::class.java)
}