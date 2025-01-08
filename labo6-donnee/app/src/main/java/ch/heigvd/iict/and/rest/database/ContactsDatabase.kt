/**
 * Nom du fichier : ContactsDatabase.kt
 * Description    : Implémente la base de données locale pour l'application, utilisant Room pour
 *                  gérer les entités et les DAO. Fournit des méthodes pour obtenir l'instance
 *                  unique de la base de données et accéder aux DAO.
 * Auteur         : ICI
 * Date           : 08 janvier 2025
 */
package ch.heigvd.iict.and.rest.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import ch.heigvd.iict.and.rest.database.converters.CalendarConverter
import ch.heigvd.iict.and.rest.models.Contact
import ch.heigvd.iict.and.rest.models.PhoneType
import java.util.Calendar
import java.util.GregorianCalendar
import kotlin.concurrent.thread

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
                val _instance = Room.databaseBuilder(context.applicationContext,
                ContactsDatabase::class.java, "contacts.db")
                    .fallbackToDestructiveMigration()
                    //.addCallback(MyDatabaseCallback()) //FIXME - can be removed
                    .build()

                INSTANCE = _instance
                _instance
            }
        }

/*        //FIXME - can be removed
        /**
         * Section commentée : MyDatabaseCallback
         * Description : Exemple d'utilisation d'un callback pour insérer des données de test
         *               lors de la création initiale de la base de données.
         */
        private class MyDatabaseCallback : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let{ database ->
                    thread {
                        if(database.contactsDao().getCount() == 0) {
                            val c1 =  Contact(  id = null,
                                                name = "Hilt",
                                                firstname = "William",
                                                birthday = GregorianCalendar.getInstance().apply {
                                                    set(Calendar.YEAR, 1997)
                                                    set(Calendar.MONTH, Calendar.DECEMBER)
                                                    set(Calendar.DAY_OF_MONTH, 16)
                                                    set(Calendar.HOUR_OF_DAY, 12)
                                                    set(Calendar.MINUTE, 0)
                                                    set(Calendar.SECOND, 0)
                                                    set(Calendar.MILLISECOND, 0)
                                                },
                                                email = "w.hilt@heig-vd.ch",
                                                address = "Route de Cheseaux 1",
                                                zip = "1400", city = "Yverdon-les-Bains",
                                                type = PhoneType.OFFICE, phoneNumber = "024 111 22 33" )

                            val c2 =  Contact(  id = null,
                                                name = "Fisher",
                                                firstname = "Brenda",
                                                birthday = GregorianCalendar.getInstance().apply {
                                                    set(Calendar.YEAR, 2001)
                                                    set(Calendar.MONTH, Calendar.JULY)
                                                    set(Calendar.DAY_OF_MONTH, 9)
                                                    set(Calendar.HOUR_OF_DAY, 12)
                                                    set(Calendar.MINUTE, 0)
                                                    set(Calendar.SECOND, 0)
                                                    set(Calendar.MILLISECOND, 0)
                                                },
                                                email = "b.fisher@heig-vd.ch",
                                                address = "Avenue des Sports 20",
                                                zip = "1400", city = "Yverdon-les-Bains",
                                                type = PhoneType.MOBILE, phoneNumber = "079 111 22 33" )

                            database.contactsDao().insert(c1)
                            database.contactsDao().insert(c2)
                        }
                    }
                }

            }
        }*/

    }

}