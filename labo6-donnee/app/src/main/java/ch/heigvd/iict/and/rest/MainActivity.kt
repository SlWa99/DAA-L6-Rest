/**
 * Nom du fichier : MainActivity.kt
 * Description    : Activité principale de l'application, gérant les interactions utilisateur
 *                  pour la création, la modification et la synchronisation des contacts.
 * Auteur         : ICI
 * Date           : 08 janvier 2025
 */
package ch.heigvd.iict.and.rest

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.viewModels
import ch.heigvd.iict.and.rest.databinding.ActivityMainBinding
import ch.heigvd.iict.and.rest.fragments.EditFragment
import ch.heigvd.iict.and.rest.viewmodels.ContactsViewModel
import ch.heigvd.iict.and.rest.viewmodels.ContactsViewModelFactory

/**
 * Classe : MainActivity
 * Description : Activité principale de l'application utilisant une architecture MVVM.
 *               Fournit une interface pour naviguer et interagir avec les fragments d'édition
 *               et les actions de synchronisation avec un serveur distant.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding : ActivityMainBinding
    private val contactsViewModel: ContactsViewModel by viewModels {
        ContactsViewModelFactory((application as ContactsApplication).repository)
    }

    /**
     * Méthode : onCreate
     * Description : Initialise l'interface utilisateur et configure le bouton flottant pour
     *               ouvrir le fragment d'édition des contacts.
     * @param savedInstanceState État enregistré de l'activité, s'il existe.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        binding.mainFabNew.setOnClickListener {
            contactsViewModel.selectContact(null) // Prépare un contact vide
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, EditFragment()) // Remplace proprement
                .addToBackStack(null) // Permet retour arrière
                .commit()
        }
    }

    /**
     * Méthode : onCreateOptionsMenu
     * Description : Gonfle le menu d'options avec les éléments définis dans `main_menu.xml`.
     * @param menu Le menu à gonfler.
     * @return Un booléen indiquant si le menu a été créé avec succès.
     */
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    /**
     * Méthode : onOptionsItemSelected
     * Description : Gère les interactions avec les éléments du menu, y compris la synchronisation
     *               des contacts avec le serveur et l'enrollment.
     * @param item L'élément de menu sélectionné.
     * @return Un booléen indiquant si l'action a été traitée.
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId) {
            R.id.menu_main_synchronize -> {
                contactsViewModel.refresh()
                true
            }
            R.id.menu_main_populate -> {
                contactsViewModel.enroll()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    /**
     * Classe compagnon : MainActivity
     * Description : Contient des constantes utiles pour la classe MainActivity, y compris un tag
     *               pour le logging.
     */
    companion object {
        private val TAG = MainActivity::class.java.simpleName
    }
}