/**
 * Nom du fichier : ApiService.kt
 * Description    : Interface Retrofit définissant les endpoints de l'API REST pour l'application.
 *                  Gère les opérations CRUD pour les contacts et l'enregistrement utilisateur.
 * Auteur         : ICI
 * Date           : 08 janvier 2025
 */
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

/**
 * Interface : ApiService
 * Description : Définit les appels réseau utilisés pour interagir avec le serveur REST.
 *               Fournit des méthodes suspendues pour les opérations d'enrollment,
 *               récupération, création, mise à jour, et suppression de contacts.
 */
interface ApiService {
    /**
     * Méthode : enroll
     * Description : Crée un nouveau jeu de données côté serveur et retourne un UUID unique.
     * @return Un `ResponseBody` contenant l'UUID attribué.
     */
    @GET("enroll")
    suspend fun enroll(): ResponseBody

    /**
     * Méthode : getContacts
     * Description : Récupère tous les contacts liés au UUID fourni.
     * @param uuid Identifiant unique du jeu de données.
     * @return Une liste de `Contact` correspondant au UUID.
     */
    @GET("contacts")
    suspend fun getContacts(@Header("X-UUID") uuid: String): List<Contact>

    /**
     * Méthode : createContact
     * Description : Envoie un nouveau contact au serveur pour le créer et le synchroniser.
     * @param uuid Identifiant unique du jeu de données.
     * @param contact Les détails du contact à créer.
     * @return Le contact créé avec ses informations à jour.
     */
    @POST("contacts")
    suspend fun createContact(
        @Header("X-UUID") uuid: String,
        @Body contact: Contact
    ): Contact

    /**
     * Méthode : updateContact
     * Description : Met à jour un contact existant sur le serveur.
     * @param uuid Identifiant unique du jeu de données.
     * @param contactId ID du contact à mettre à jour.
     * @param contact Les informations mises à jour du contact.
     * @return Le contact mis à jour.
     */
    @PUT("contacts/{id}")
    suspend fun updateContact(
        @Header("X-UUID") uuid: String,
        @Path("id") contactId: Long,
        @Body contact: Contact
    ): Contact

    /**
     * Méthode : deleteContact
     * Description : Supprime un contact sur le serveur à partir de son ID.
     * @param uuid Identifiant unique du jeu de données.
     * @param contactId ID du contact à supprimer.
     */
    @DELETE("contacts/{id}")
    suspend fun deleteContact(
        @Header("X-UUID") uuid: String,
        @Path("id") contactId: Long
    )
}