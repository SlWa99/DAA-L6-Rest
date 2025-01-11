/**
 * Nom du fichier : ContactsViewModel.kt
 * Description    : Implémente le ViewModel pour gérer les opérations sur les contacts,
 *                  y compris la synchronisation avec le serveur et la gestion locale.
 * Auteur         : Bugna, Slimani & Steiner
 * Date           : 08 janvier 2025
 */
package ch.heigvd.iict.and.rest.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import ch.heigvd.iict.and.rest.ContactsRepository
import ch.heigvd.iict.and.rest.models.Contact
import kotlinx.coroutines.launch

/**
 * Classe : ContactsViewModel
 * Description : Classe ViewModel gérant les contacts pour une application Android. Elle
 *               centralise les actions sur la base de données locale et les interactions
 *               avec un service REST distant.
 * @param repository Instance du repository pour accéder aux données des contacts.
 */
class ContactsViewModel(private val repository: ContactsRepository) : ViewModel() {

    // Liste de tous les contacts synchronisés avec la base locale.
    val allContacts = repository.allContacts
    // Contact actuellement sélectionné (peut être null).
    private val _selectedContact = MutableLiveData<Contact?>()
    val selectedContact: LiveData<Contact?> = _selectedContact

    /**
     * Méthode : selectContact
     * Description : Permet de sélectionner un contact (existant ou nouveau).
     * @param contact Le contact à sélectionner ou null pour désélectionner.
     */
    fun selectContact(contact: Contact?) {
        _selectedContact.value = contact
    }

    /**
     * Méthode : saveContact
     * Description : Sauvegarde un contact dans la base de données locale.
     * @param contact Le contact à sauvegarder (nouveau ou modifié).
     */
    fun saveContact(contact: Contact) {
        viewModelScope.launch {
            if (contact.id?.toInt() == null) {
                repository.insert(contact) // Création
            } else {
                repository.update(contact) // Modification
            }
        }
    }

    /**
     * Méthode : enroll
     * Description : Récupère un UUID, remplace les données locales, et synchronise les contacts.
     */
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

    /**
     * Méthode : refresh
     * Description : Rafraîchit les contacts en récupérant les données du serveur.
     */
    fun refresh() {        viewModelScope.launch {
        try {
            repository.synchronizeDirtyContacts()
            // Ne pas appeler refresh() ici !
        } catch (e: Exception) {
            Log.e("ContactsViewModel", "Erreur lors de la synchronisation", e)
        }
    }
    }

    /**
     * Méthode : updateContact
     * Description : Met à jour un contact localement et rafraîchit la liste.
     * @param contact Le contact à mettre à jour.
     */
    fun updateContact(contact: Contact) = viewModelScope.launch {
        repository.update(contact)
        refresh() // Mise à jour de la liste après édition
    }

    /**
     * Méthode : deleteContact
     * Description : Supprime un contact localement.
     * @param contact Le contact à supprimer.
     */
    fun deleteContact(contact: Contact) {
        viewModelScope.launch {
            try {
                repository.delete(contact)
            } catch (e: Exception) { // Gère l'erreur si nécessaire
                Log.e("ContactsViewModel", "Erreur lors de la suppression", e)
            }
        }
    }

    /**
     * Méthode : synchronizeDirtyContacts
     * Description : Synchronise les contacts marqués comme "dirty" avec le serveur distant.
     */
    fun synchronizeDirtyContacts() {
        viewModelScope.launch {
            try {
                repository.synchronizeDirtyContacts()
                // Ne pas appeler refresh() ici !
            } catch (e: Exception) {
                Log.e("ContactsViewModel", "Erreur lors de la synchronisation", e)
            }
        }
    }
}

/**
 * Classe : ContactsViewModelFactory
 * Description : Factory pour instancier ContactsViewModel avec un repository donné.
 * @param repository Instance du repository utilisé pour gérer les contacts.
 */
class ContactsViewModelFactory(private val repository: ContactsRepository)
    : ViewModelProvider.Factory {
    /**
     * Méthode : create
     * Description : Crée une instance de ContactsViewModel à partir du repository fourni.
     * @param modelClass La classe du ViewModel à instancier.
     * @return Une instance de ContactsViewModel.
     * @throws IllegalArgumentException Si la classe donnée ne correspond pas à ContactsViewModel.
     */
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ContactsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ContactsViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}