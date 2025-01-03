package ch.heigvd.iict.and.rest

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import ch.heigvd.iict.and.rest.database.ContactsDao
import ch.heigvd.iict.and.rest.models.Contact
import ch.heigvd.iict.and.rest.network.ApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import com.google.gson.Gson

class ContactsRepository(
    private val contactsDao: ContactsDao,
    private val apiService: ApiService,
    private val context: Context
)  {

    val allContacts = contactsDao.getAllContactsLiveData()

    companion object {
        private val TAG = "ContactsRepository"
    }

    // ----------------------------------
    // Gestion des SharedPreferences pour stocker le UUID
    // ----------------------------------
    private val sharedPreferences = context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)

    private fun saveUuid(uuid: String) {
        sharedPreferences.edit().putString("uuid_key", uuid).apply()
    }

    private fun getUuid(): String? {
        return sharedPreferences.getString("uuid_key", null)
    }

    // ----------------------------------
    // Récupérer un contact par ID
    // ----------------------------------
    fun getContactById(id: Long): LiveData<Contact> = contactsDao.getContactById(id)
    suspend fun deleteContact(contact: Contact) = withContext(Dispatchers.IO) {
        contactsDao.delete(contact)
    }


    // ----------------------------------
    // Insérer un contact avec synchronisation
    // ----------------------------------
    suspend fun insert(contact: Contact) = withContext(Dispatchers.IO) {
        try {
            // Marquer comme dirty initialement
            contact.isDirty = true
            contact.lastModified = System.currentTimeMillis()

            // Sauvegarder localement
            val localId = contactsDao.insert(contact)

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

    // ----------------------------------
    // Mettre à jour un contact avec synchronisation
    // ----------------------------------
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

    // Supprime un contact
    // Dans ContactsRepository
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

    // Supprime tous les contacts de la base de données
    suspend fun deleteAllContacts() = withContext(Dispatchers.IO) {
        contactsDao.clearAllContacts()
        contactsDao.resetPrimaryKey()
    }

    // ----------------------------------
    // Synchronisation manuelle des contacts "dirty"
    // ----------------------------------
    suspend fun synchronizeDirtyContacts() = withContext(Dispatchers.IO) {
        val uuid = getUuid() ?: return@withContext
        Log.d(TAG, "Synchronisation lancée avec UUID: $uuid") // Ajoutez ce log

        val dirtyContacts = getDirtyContacts()
        Log.d(TAG, "Nombre de contacts dirty : ${dirtyContacts.size}") // Affiche le nombre de contacts à synchroniser

        dirtyContacts.forEach { contact ->
            try {
                if (contact.id == null) {
                    Log.d(TAG, "Création d'un nouveau contact sur le serveur : ${contact.name}")
                    apiService.createContact(uuid, contact)
                } else {
                    Log.d(TAG, "Mise à jour d'un contact existant : ${contact.id}")
                    apiService.updateContact(uuid, contact.id!!, contact)
                }
                contact.isDirty = false
                contactsDao.update(contact)
                Log.d(TAG, "Contact synchronisé avec succès : ${contact.id}")
            } catch (e: Exception) {
                Log.e(TAG, "Échec de synchronisation pour le contact ${contact.id}", e)
            }
        }
    }

    // Récupère les contacts depuis le serveur (simulation pour l'instant)
    suspend fun fetchContactsFromServer(): List<Contact> = withContext(Dispatchers.IO) {
        val uuid = getUuid() ?: throw IllegalStateException("UUID manquant")
        return@withContext apiService.getContacts(uuid)
    }

    suspend fun markAsDirty(contact: Contact) {
        contact.isDirty = true
        contact.lastModified = System.currentTimeMillis()
        update(contact) // Appelle la méthode existante pour mettre à jour le contact
    }

    suspend fun getDirtyContacts(): List<Contact> {
        return contactsDao.getDirtyContacts() // Appelle la requête SQL définie dans ContactDao
    }

    // Fonction d'enrollment
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
}