package ch.heigvd.iict.and.rest

import android.app.Application
import ch.heigvd.iict.and.rest.database.ContactsDatabase
import ch.heigvd.iict.and.rest.network.RetrofitClient

class ContactsApplication : Application() {
    val database by lazy { ContactsDatabase.getDatabase(this) }
    val repository by lazy {
        ContactsRepository(
            contactsDao = database.contactsDao(),
            apiService = RetrofitClient.apiService,
            context = applicationContext
        )
    }
}