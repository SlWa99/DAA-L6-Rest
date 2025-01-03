package ch.heigvd.iict.and.rest

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import ch.heigvd.iict.and.rest.database.ContactsDao
import ch.heigvd.iict.and.rest.models.Contact
import ch.heigvd.iict.and.rest.network.ApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ContactsRepository(
    private val contactsDao: ContactsDao,
    private val apiService: ApiService,
    private val context: Context
)  {

    val allContacts = contactsDao.getAllContactsLiveData()

    companion object {
        private val TAG = "ContactsRepository"
    }

    // Gestion des SharedPreferences pour stocker le UUID
    private val sharedPreferences = context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)

    private fun saveUuid(uuid: String) {
        sharedPreferences.edit().putString("uuid_key", uuid).apply()
    }

    private fun getUuid(): String? {
        return sharedPreferences.getString("uuid_key", null)
    }

    fun getContactById(id: Long): LiveData<Contact> = contactsDao.getContactById(id)
    suspend fun deleteContact(contact: Contact) = withContext(Dispatchers.IO) {
        contactsDao.delete(contact)
    }

    // Insère un nouveau contact
    suspend fun insert(contact: Contact) = withContext(Dispatchers.IO) {
        contactsDao.insert(contact) // Ajoute dans la base locale
    }

    // Met à jour un contact existant
    suspend fun update(contact: Contact) = withContext(Dispatchers.IO) {
        contactsDao.update(contact) // Modifie dans la base locale
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
            val uuid = apiService.enroll()
            Log.d("ServerResponse", "UUID reçu : $uuid")
            saveUuid(uuid)

            // Nettoyage des contacts existants
            deleteAllContacts()

            // Récupération des contacts
            val serverContacts = apiService.getContacts(uuid)
            serverContacts.forEach { contact ->
                // Assurez-vous que les champs sont correctement initialisés
                val localContact = contact.copy(
                    id = null,  // L'ID sera généré localement
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