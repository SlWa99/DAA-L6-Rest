package ch.heigvd.iict.and.rest

import androidx.lifecycle.LiveData
import ch.heigvd.iict.and.rest.database.ContactsDao
import ch.heigvd.iict.and.rest.models.Contact
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ContactsRepository(private val contactsDao: ContactsDao) {

    val allContacts = contactsDao.getAllContactsLiveData()

    companion object {
        private val TAG = "ContactsRepository"
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
    suspend fun delete(contact: Contact) = withContext(Dispatchers.IO) {
        contactsDao.delete(contact) // Supprime dans la base locale
    }

    // Supprime tous les contacts de la base de données
    suspend fun deleteAllContacts() = withContext(Dispatchers.IO) {
        contactsDao.clearAllContacts()
        contactsDao.resetPrimaryKey()
    }

    // Récupère les contacts depuis le serveur (simulation pour l'instant)
    suspend fun fetchContactsFromServer(): List<Contact> = withContext(Dispatchers.IO) {
        // Pour l'instant, on simule une réponse du serveur avec des données de test
        return@withContext listOf(
            Contact(
                id = null,
                name = "Server",
                firstname = "Test",
                birthday = null,
                email = "server.test@example.com",
                address = "Server Street 1",
                zip = "1234",
                city = "ServerCity",
                type = null,
                phoneNumber = "123456789"
            )
        )
    }
}