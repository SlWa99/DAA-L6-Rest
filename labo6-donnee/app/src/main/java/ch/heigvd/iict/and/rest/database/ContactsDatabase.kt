/**
 * Nom du fichier : ContactsDatabase.kt
 * Description    : Implémente la base de données locale pour l'application, utilisant Room pour
 *                  gérer les entités et les DAO. Fournit des méthodes pour obtenir l'instance
 *                  unique de la base de données et accéder aux DAO.
 * Auteur         : Bugna, Slimani & Steiner
 * Date           : 08 janvier 2025
 */
package ch.heigvd.iict.and.rest.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import ch.heigvd.iict.and.rest.database.converters.CalendarConverter
import ch.heigvd.iict.and.rest.models.Contact

/**
 * Annotation : @Database
 * Description : Définit la base de données Room. Elle inclut les entités et leur version actuelle.
 *               La migration destructive est activée par défaut.
 */
@Database(entities = [Contact::class], version = 2, exportSchema = true)
/**
 * Annotation : @TypeConverters
 * Description : Spécifie les convertisseurs personnalisés, comme pour les champs de type `Calendar`.
 */
@TypeConverters(CalendarConverter::class)
/**
 * Classe : ContactsDatabase
 * Description : Classe abstraite représentant la base de données Room. Inclut des entités comme
 *               `Contact` et utilise un convertisseur pour gérer des types complexes.
 */
abstract class ContactsDatabase : RoomDatabase() {

    /**
     * Méthode : contactsDao
     * Description : Fournit une instance de `ContactsDao` pour effectuer des opérations CRUD
     *               sur la table des contacts.
     * @return Une instance de `ContactsDao`.
     */
    abstract fun contactsDao() : ContactsDao
    /**
     * Classe compagnon : ContactsDatabase
     * Description : Fournit un singleton thread-safe pour accéder à la base de données.
     */
    companion object {

        @Volatile
        private var INSTANCE : ContactsDatabase? = null

        /**
        * Méthode : getDatabase
        * Description : Retourne l'instance unique de la base de données. Si elle n'existe pas encore,
        *               elle est créée en utilisant le pattern Singleton et la méthode
        *               `synchronized` pour la sécurité thread.
        * @param context Le contexte de l'application, utilisé pour construire la base de données.
        * @return Une instance unique de `ContactsDatabase`.
        */
        fun getDatabase(context: Context) : ContactsDatabase {

            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(context.applicationContext,
                ContactsDatabase::class.java, "contacts.db")
                    .fallbackToDestructiveMigration()
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}