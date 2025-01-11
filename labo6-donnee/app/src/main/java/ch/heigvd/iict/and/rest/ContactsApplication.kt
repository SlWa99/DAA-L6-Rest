/**
 * Nom du fichier : ContactsApplication.kt
 * Description    : Classe `Application` personnalisée pour initialiser les instances
 *                  de la base de données et du repository. Gère le cycle de vie global
 *                  des composants de l'application.
 * Auteur         : Bugna, Slimani & Steiner
 * Date           : 08 janvier 2025
 */
package ch.heigvd.iict.and.rest

import android.app.Application
import ch.heigvd.iict.and.rest.database.ContactsDatabase
import ch.heigvd.iict.and.rest.network.RetrofitClient

/**
 * Classe : ContactsApplication
 * Description : Fournit un point d'entrée pour initialiser les dépendances de l'application,
 *               notamment la base de données Room et le repository. Utilisée pour un accès
 *               global à ces composants.
 */
class ContactsApplication : Application() {
    /**
     * Propriété : database
     * Description : Instance unique de la base de données locale, initialisée à la demande.
     */
    private val database by lazy { ContactsDatabase.getDatabase(this) }

    /**
     * Propriété : repository
     * Description : Instance unique de `ContactsRepository`, initialisée avec
     *               la DAO et le client API nécessaires.
     */
    val repository by lazy {
        ContactsRepository(
            contactsDao = database.contactsDao(),
            apiService = RetrofitClient.apiService,
            context = applicationContext
        )
    }
}