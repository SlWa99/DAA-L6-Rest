/**
 * Nom du fichier : ContactsRepository.kt
 * Description    : Implémente un repository pour gérer les opérations sur les contacts,
 *                  incluant l'accès à la base de données locale et les interactions avec
 *                  le service API distant pour la synchronisation des données.
 * Auteur         : Bugna, Slimani & Steiner
 * Date           : 08 janvier 2025
 */
package ch.heigvd.iict.and.rest

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import ch.heigvd.iict.and.rest.database.ContactsDao
import ch.heigvd.iict.and.rest.models.Contact
import ch.heigvd.iict.and.rest.network.ApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Classe : ContactsRepository
 * Description : Gère les contacts via une base de données locale et une API REST. Fournit des
 *               méthodes pour la synchronisation, la gestion des SharedPreferences, et le CRUD
 *               des contacts.
 * @param contactsDao DAO pour accéder à la base de données locale.
 * @param apiService Service API pour les opérations réseau.
 * @param context Contexte pour accéder aux ressources et préférences partagées.
 */
class ContactsRepository(
    private val contactsDao: ContactsDao,
    private val apiService: ApiService,
    private val context: Context
)  {

    val allContacts = contactsDao.getAllContactsLiveData()

    // ----------------------------------
    // Gestion des SharedPreferences pour stocker le UUID
    // ----------------------------------
    private val sharedPreferences = context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)

    /**
     * Méthode : saveUuid
     * Description : Enregistre le UUID dans les SharedPreferences.
     * @param uuid Identifiant unique reçu du serveur.
     */
    private fun saveUuid(uuid: String) {
        sharedPreferences.edit().putString("uuid_key", uuid).apply()
    }

    /**
     * Méthode : getUuid
     * Description : Récupère le UUID stocké dans les SharedPreferences.
     * @return Le UUID sous forme de chaîne ou null s'il n'existe pas.
     */
    private fun getUuid(): String? {
        return sharedPreferences.getString("uuid_key", null)
    }

    /**
     * Méthode : insert
     * Description : Insère un contact dans la base de données locale et tente
     *               de le synchroniser avec le serveur.
     * @param contact Le contact à insérer.
     */
    suspend fun insert(contact: Contact) = withContext(Dispatchers.IO) {
        try {
            // Marquer comme dirty initialement
            contact.isDirty = true
            contact.lastModified = System.currentTimeMillis()

            // Sauvegarder localement
            contactsDao.insert(contact)

            // Tenter la synchronisation avec le serveur
            try {
                val uuid = getUuid() ?: throw IllegalStateException("UUID manquant")

                // Préparer le contact pour l'envoi
                val contactToSend = contact.copy(
                    id = null,
                    uuid = null,
                    isDirty = false,
                    lastModified = 0
                )

                val serverResponse = apiService.createContact(uuid, contactToSend)

                // Mise à jour locale si succès
                contact.isDirty = false
                contact.uuid = serverResponse.uuid
                contactsDao.update(contact)

            } catch (e: Exception) {
                Log.e(TAG, "Échec de la synchronisation lors de l'insertion", e)
                // Le contact reste marqué comme dirty
            }
        } catch (e: Exception) {
            Log.e(TAG, "Erreur lors de l'insertion", e)
            throw e
        }
    }

    /**
     * Méthode : update
     * Description : Met à jour un contact dans la base de données locale et tente
     *               de synchroniser les modifications avec le serveur.
     * @param contact Le contact à mettre à jour.
     */
    suspend fun update(contact: Contact) = withContext(Dispatchers.IO) {
        try {
            contact.isDirty = true
            contact.lastModified = System.currentTimeMillis()
            contactsDao.update(contact)

            try {
                val uuid = getUuid() ?: throw IllegalStateException("UUID manquant")
                apiService.updateContact(uuid, contact.id!!, contact)
                contact.isDirty = false
                contactsDao.update(contact)
            } catch (e: Exception) {
                Log.e(TAG, "Échec de la synchronisation lors de la mise à jour", e)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Erreur lors de la mise à jour", e)
            throw e
        }
    }

    /**
     * Méthode : delete
     * Description : Supprime un contact localement et tente la suppression
     *               sur le serveur si applicable.
     * @param contact Le contact à supprimer.
     */
    suspend fun delete(contact: Contact) = withContext(Dispatchers.IO) {
        Log.d(TAG, "Début de la suppression du contact ${contact.id}")
        try {
            // Suppression locale
            contactsDao.delete(contact)
            Log.d(TAG, "Suppression locale effectuée")

            // Récupérer l'UUID stocké
            val uuid = getUuid() ?: return@withContext

            // Si le contact a un ID serveur, tenter la suppression sur le serveur
            if (contact.id != null) {
                try {
                    apiService.deleteContact(uuid, contact.id!!)
                } catch (e: Exception) {
                    Log.e(TAG, "Erreur lors de la suppression sur le serveur", e)
                    // Optionnel : gérer l'échec de la suppression sur le serveur
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Erreur lors de la suppression", e)
            throw e
        }
    }

    /**
     * Méthode : deleteAllContacts
     * Description : Supprime tous les contacts locaux de la base de données.
     */
    private suspend fun deleteAllContacts() = withContext(Dispatchers.IO) {
        contactsDao.clearAllContacts()
        contactsDao.resetPrimaryKey()
    }

    /**
     * Méthode : synchronizeDirtyContacts
     * Description : Synchronise manuellement les contacts marqués comme
     *               "dirty" avec le serveur distant.
     */
    suspend fun synchronizeDirtyContacts() = withContext(Dispatchers.IO) {
        val uuid = getUuid() ?: return@withContext
        Log.d(TAG, "Début de la synchronisation")

        try {
            // Récupérer uniquement les contacts dirty
            val dirtyContacts = getDirtyContacts()
            Log.d(TAG, "Contacts dirty à synchroniser : ${dirtyContacts.size}")

            dirtyContacts.forEach { dirtyContact ->
                try {
                    // Contact existant - faire un PUT
                    if (dirtyContact.id != null && dirtyContact.uuid != null) {
                        Log.d(TAG, "Mise à jour du contact existant ${dirtyContact.id}")
                        val contactToUpdate = dirtyContact.copy(isDirty = false)
                        apiService.updateContact(uuid, dirtyContact.id!!, contactToUpdate)
                    }
                    // Nouveau contact - faire un POST
                    else {
                        Log.d(TAG, "Création d'un nouveau contact")
                        val contactToCreate = dirtyContact.copy(
                            id = null,
                            uuid = null,
                            isDirty = false
                        )
                        val response = apiService.createContact(uuid, contactToCreate)
                        dirtyContact.uuid = response.uuid // Mettre à jour l'UUID local
                    }

                    // Marquer comme non-dirty dans la DB locale
                    dirtyContact.isDirty = false
                    contactsDao.update(dirtyContact)
                    Log.d(TAG, "Contact synchronisé avec succès")
                } catch (e: Exception) {
                    Log.e(TAG, "Erreur lors de la synchronisation du contact", e)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Erreur générale de synchronisation", e)
            throw e
        }
    }

    /**
     * Méthode : getDirtyContacts
     * Description : Récupère les contacts marqués comme "dirty" depuis la base de données locale.
     * @return Une liste de contacts non synchronisés.
     */
    private fun getDirtyContacts(): List<Contact> {
        return contactsDao.getDirtyContacts() // Appelle la requête SQL définie dans ContactDao
    }

    /**
     * Méthode : enroll
     * Description : Obtient un nouveau UUID du serveur, remplace les données locales,
     *               et synchronise les contacts avec les données du serveur.
     */
    suspend fun enroll() = withContext(Dispatchers.IO) {
        try {
            // Obtenir le UUID
            val response = apiService.enroll()
            val uuid = response.string().trim()  // Convertir le ResponseBody en String
            Log.d("ServerResponse", "UUID reçu : $uuid")
            saveUuid(uuid)

            // Nettoyage des contacts existants
            deleteAllContacts()

            // Récupération des contacts
            val serverContacts = apiService.getContacts(uuid)
            serverContacts.forEach { contact ->
                val localContact = contact.copy(
                    id = null,
                    isDirty = false,
                    lastModified = System.currentTimeMillis()
                )
                contactsDao.insert(localContact)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Erreur lors de l'enrollment", e)
            throw e
        }
    }

    /**
     * Classe compagnon : ContactsRepository
     * Description : Fournit des constantes et des méthodes utilitaires liées au repository,
     *               comme des tags pour le logging.
     */
    companion object {
        private const val TAG = "ContactsRepository"
    }
}