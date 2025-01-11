/**
 * Nom du fichier : ListFragment.kt
 * Description    : Fragment affichant la liste des contacts. Permet à l'utilisateur de visualiser
 *                  les contacts existants et d'accéder à leur édition via un clic sur un élément.
 * Auteur         : Bugna, Slimani & Steiner
 * Date           : 08 janvier 2025
 */
package ch.heigvd.iict.and.rest.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import ch.heigvd.iict.and.rest.ContactsApplication
import ch.heigvd.iict.and.rest.R
import ch.heigvd.iict.and.rest.databinding.FragmentListBinding
import ch.heigvd.iict.and.rest.viewmodels.ContactsViewModel
import ch.heigvd.iict.and.rest.viewmodels.ContactsViewModelFactory

/**
 * Classe : ListFragment
 * Description : Fragment gérant l'affichage et les interactions avec la liste des contacts.
 *               Utilise un RecyclerView pour afficher les données et navigue vers le
 *               fragment d'édition au besoin.
 */
class ListFragment : Fragment() {

    private lateinit var binding : FragmentListBinding

    private val contactsViewModel: ContactsViewModel by activityViewModels {
        ContactsViewModelFactory(((requireActivity().application as ContactsApplication).repository))
    }

    /**
     * Méthode : onCreateView
     * Description : Crée et retourne la vue associée à ce fragment. Initialise le binding
     *               pour interagir avec les éléments du layout.
     * @param inflater Utilisé pour gonfler la vue du fragment.
     * @param container Le conteneur auquel attacher la vue.
     * @param savedInstanceState État enregistré précédent, s'il existe.
     * @return La vue gonflée pour ce fragment.
     */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentListBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    /**
     * Méthode : onViewCreated
     * Description : Configure la RecyclerView et observe les données des contacts depuis le ViewModel.
     *               Gère également les clics sur les éléments pour naviguer vers le fragment d'édition.
     * @param view La vue créée pour ce fragment.
     * @param savedInstanceState État enregistré précédent, s'il existe.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = ContactsAdapter(emptyList()) { _, _, _, id ->
            // we locate the contact to edit
            if(contactsViewModel.allContacts.value != null) {
                val selectedContact = contactsViewModel.allContacts.value!!.find { it.id == id }
                if(selectedContact != null) {
                    //FIXME - user clicks on selectedContact, we want to edit it
                    // Toast.makeText(requireActivity(), "TODO - Edition de ${selectedContact.firstname} ${selectedContact.name}", Toast.LENGTH_SHORT).show()
                    // Mettre à jour le contact sélectionné dans le ViewModel
                    contactsViewModel.selectContact(selectedContact)

                    // Naviguer vers EditFragment
                    parentFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, EditFragment.newInstance())
                        .addToBackStack(null)
                        .commit()
                }
            }
        }
        binding.listRecycler.adapter = adapter
        binding.listRecycler.layoutManager = LinearLayoutManager(requireContext())

        contactsViewModel.allContacts.observe(viewLifecycleOwner) { updatedContacts ->
            adapter.contacts = updatedContacts
            // we display an "empty view" when adapter contains no contact
            if(updatedContacts.isEmpty()) {
                binding.listRecycler.visibility = View.GONE
                binding.listContentEmpty.visibility = View.VISIBLE
            }
            else {
                binding.listContentEmpty.visibility = View.GONE
                binding.listRecycler.visibility = View.VISIBLE
            }
        }

    }

    /**
     * Classe compagnon : ListFragment
     * Description : Fournit des constantes et des méthodes statiques associées à ListFragment,
     *               y compris une méthode pour instancier ce fragment.
     */
    companion object {
        @JvmStatic
        fun newInstance() =
            ListFragment()

        private val TAG = ListFragment::class.java.simpleName
    }

}