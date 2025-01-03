package ch.heigvd.iict.and.rest.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import ch.heigvd.iict.and.rest.ContactsRepository
import ch.heigvd.iict.and.rest.models.Contact
import ch.heigvd.iict.and.rest.models.PhoneType
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.GregorianCalendar

class ContactsViewModel(private val repository: ContactsRepository) : ViewModel() {

    val allContacts = repository.allContacts
    // Contact sélectionné
    private val _selectedContact = MutableLiveData<Contact?>()
    val selectedContact: LiveData<Contact?> = _selectedContact

    // Sélectionne un contact (existant ou nouveau)
    fun selectContact(contact: Contact?) {
        _selectedContact.value = contact
    }

    // Sauvegarde un contact (création ou modification)
    fun saveContact(contact: Contact) {
        viewModelScope.launch {
            if (contact.id?.toInt() == null) {
                repository.insert(contact) // Création
            } else {
                repository.update(contact) // Modification
            }
        }
    }

    // actions
    fun enroll() {
        viewModelScope.launch {
            try {
                repository.enroll() // Appelle la fonction enroll dans le repository
                // Vous pouvez ajouter un LiveData pour notifier d'un succès
            } catch (e: Exception) {
                // Gère les erreurs et les affiche dans les logs
                Log.e("ContactsViewModel", "Erreur lors de l'enrollment", e)
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            // TODO
            try {
                // 1. Récupérer les contacts du backend
                val remoteContacts = repository.fetchContactsFromServer()

                // 2. Supprimer tous les contacts locaux
                repository.deleteAllContacts()

                // 3. Insérer les nouveaux contacts
                remoteContacts.forEach { contact ->
                    repository.insert(contact)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                // TODO: Gérer l'erreur (par exemple avec un LiveData<Error>)

            }
        }
    }
    fun updateContact(contact: Contact) = viewModelScope.launch {
        repository.update(contact)
        refresh() // Mise à jour de la liste après édition
    }

    fun deleteContact(contact: Contact) {
        viewModelScope.launch {
            try {
                repository.delete(contact)
            } catch (e: Exception) {
                Log.e("ContactsViewModel", "Erreur lors de la suppression", e)
                // Gérer l'erreur si nécessaire
            }
        }
    }
    fun synchronizeDirtyContacts() {
        viewModelScope.launch {
            try {
                repository.synchronizeDirtyContacts()
            } catch (e: Exception) {
                Log.e("ContactsViewModel", "Erreur lors de la synchronisation", e)
            }
        }
    }
}

class ContactsViewModelFactory(private val repository: ContactsRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ContactsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ContactsViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}