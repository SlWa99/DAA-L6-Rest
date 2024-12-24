package ch.heigvd.iict.and.rest.viewmodels

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
            if (contact.id?.toInt() == 0) {
                repository.insert(contact) // Création
            } else {
                repository.update(contact) // Modification
            }
        }
    }

    // actions
    fun enroll() {
        viewModelScope.launch {
            // TODO

            // Création de quelques contacts de test
            val contacts = listOf(
                Contact(
                    id = null,
                    name = "Dupont",
                    firstname = "Jean",
                    birthday = GregorianCalendar.getInstance().apply {
                        set(Calendar.YEAR, 1990)
                        set(Calendar.MONTH, Calendar.JANUARY)
                        set(Calendar.DAY_OF_MONTH, 15)
                        set(Calendar.HOUR_OF_DAY, 12)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    },
                    email = "jean.dupont@example.com",
                    address = "Rue de la Paix 1",
                    zip = "1004",
                    city = "Lausanne",
                    type = PhoneType.MOBILE,
                    phoneNumber = "079 123 45 67"
                ),
                Contact(
                    id = null,
                    name = "Martin",
                    firstname = "Sophie",
                    birthday = GregorianCalendar.getInstance().apply {
                        set(Calendar.YEAR, 1985)
                        set(Calendar.MONTH, Calendar.MARCH)
                        set(Calendar.DAY_OF_MONTH, 22)
                        set(Calendar.HOUR_OF_DAY, 12)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    },
                    email = "sophie.martin@example.com",
                    address = "Avenue des Alpes 15",
                    zip = "1400",
                    city = "Yverdon",
                    type = PhoneType.OFFICE,
                    phoneNumber = "024 987 65 43"
                )
            )

            // Insertion des contacts dans la base de données
            contacts.forEach { contact ->
                repository.insert(contact)  // Utilisation de la méthode suspend
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