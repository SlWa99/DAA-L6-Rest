/**
 * Nom du fichier : EditFragment.kt
 * Description    : Fragment permettant la création, la modification, et la suppression de contacts.
 *                  Gère les interactions de l'utilisateur avec les champs du formulaire.
 * Auteur         : Bugna, Slimani & Steiner
 * Date           : 08 janvier 2025
 */

package ch.heigvd.iict.and.rest.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import ch.heigvd.iict.and.rest.ContactsApplication
import ch.heigvd.iict.and.rest.R
import ch.heigvd.iict.and.rest.databinding.FragmentEditBinding
import ch.heigvd.iict.and.rest.models.Contact
import ch.heigvd.iict.and.rest.models.PhoneType
import ch.heigvd.iict.and.rest.viewmodels.ContactsViewModel
import ch.heigvd.iict.and.rest.viewmodels.ContactsViewModelFactory

/**
 * Classe : EditFragment
 * Description : Fragment dédié à l'édition ou la création de contacts. Il fournit un
 *               formulaire avec des boutons pour gérer les contacts localement.
 */
class EditFragment : Fragment() {

    private lateinit var binding: FragmentEditBinding

    private val contactsViewModel: ContactsViewModel by activityViewModels {
        ContactsViewModelFactory((requireActivity().application as ContactsApplication).repository)
    }

    /**
     * Méthode : onCreateView
     * Description : Crée et retourne la vue associée à ce fragment.
     * @param inflater Utilisé pour gonfler la vue.
     * @param container Le conteneur auquel attacher la vue.
     * @param savedInstanceState État enregistré précédent, s'il existe.
     * @return La vue gonflée pour ce fragment.
     */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentEditBinding.inflate(inflater, container, false)
        return binding.root
    }

    /**
     * Méthode : onViewCreated
     * Description : Configure les boutons et initialise le mode du fragment (création ou édition)
     *               en fonction du contact sélectionné.
     * @param view La vue créée pour ce fragment.
     * @param savedInstanceState État enregistré précédent, s'il existe.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Configurer les clics sur les boutons
        setupButtons()

        // Observer le contact sélectionné pour déterminer le mode
        contactsViewModel.selectedContact.observe(viewLifecycleOwner) { contact ->
            if (contact != null) {
                // Mode édition
                setupEditMode(contact)
            } else {
                // Mode création
                setupCreateMode()
            }
        }
    }

    /**
     * Méthode : setupButtons
     * Description : Configure les actions des boutons (Create, Save, Delete, Cancel).
     */
    private fun setupButtons() {
        // Bouton Create (nouveau contact)
        binding.editCreate.setOnClickListener {
            val newContact = createContactFromInputs()
            if (newContact != null) {
                contactsViewModel.saveContact(newContact)
                parentFragmentManager.popBackStack()
            }
        }

        // Bouton Save (modification)
        binding.editSave.setOnClickListener {
            val newContact = createContactFromInputs()
            if (newContact != null) {
            val updatedContact = newContact.copy(
                id = contactsViewModel.selectedContact.value?.id
                        )
            contactsViewModel.saveContact(updatedContact)
            parentFragmentManager.popBackStack()
            }
        }

        // Bouton Delete
        binding.editDelete.setOnClickListener {
            contactsViewModel.selectedContact.value?.let { contact ->
                contactsViewModel.deleteContact(contact) // Pas besoin de lifecycleScope ici
                Toast.makeText(context, "Contact supprimé", Toast.LENGTH_SHORT).show()
                parentFragmentManager.popBackStack()
            }
        }

        // Bouton Cancel (commun aux deux modes)
        binding.editCancel.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }

    /**
     * Méthode : setupCreateMode
     * Description : Configure le fragment en mode création. Modifie l'interface utilisateur pour
     *               afficher les options appropriées (Create, Cancel) et masque les autres.
     */
    private fun setupCreateMode() {
        binding.editTitle.text = getString(R.string.fragment_detail_title_new)
        // Afficher uniquement Create et Cancel
        binding.editCreate.visibility = View.VISIBLE
        binding.editSave.visibility = View.GONE
        binding.editDelete.visibility = View.GONE
    }

    /**
     * Méthode : setupEditMode
     * Description : Configure le fragment en mode édition. Remplit les champs du formulaire avec
     *               les données du contact existant et ajuste l'interface pour afficher Save et Delete.
     * @param contact Le contact actuellement sélectionné pour l'édition.
     */
    private fun setupEditMode(contact: Contact) {
        binding.editTitle.text = getString(R.string.fragment_detail_title_edit)
        // Remplir les champs avec les données du contact
        binding.editName.setText(contact.name)
        binding.editFirstname.setText(contact.firstname)
        binding.editEmail.setText(contact.email)
        binding.editAddress.setText(contact.address)
        binding.editZip.setText(contact.zip)
        binding.editCity.setText(contact.city)
        binding.editPhone.setText(contact.phoneNumber)

        // Sélectionner le type de téléphone
        when (contact.type) {
            PhoneType.HOME -> binding.editPhoneTypeHome.isChecked = true
            PhoneType.MOBILE -> binding.editPhoneTypeMobile.isChecked = true
            PhoneType.OFFICE -> binding.editPhoneTypeOffice.isChecked = true
            PhoneType.FAX -> binding.editPhoneTypeFax.isChecked = true
            else -> {}
        }

        // Afficher uniquement Save, Delete et Cancel
        binding.editCreate.visibility = View.GONE
        binding.editSave.visibility = View.VISIBLE
        binding.editDelete.visibility = View.VISIBLE
    }

    /**
     * Méthode : createContactFromInputs
     * Description : Génère un objet Contact à partir des valeurs saisies dans le formulaire.
     * @return Un objet Contact ou null si les champs obligatoires ne sont pas remplis.
     */
    private fun createContactFromInputs(): Contact? {
        val name = binding.editName.text.toString().trim()
        if (name.isBlank()) {
            binding.editName.error = "Le nom est obligatoire"
            Toast.makeText(context, "Le nom est obligatoire", Toast.LENGTH_SHORT).show()
            return null
            }
        return Contact(
            id = null, // sera remplacé par l'ID existant en mode édition
            name = binding.editName.text.toString(),
            firstname = binding.editFirstname.text.toString(),
            email = binding.editEmail.text.toString(),
            birthday = null, // À gérer si nécessaire
            address = binding.editAddress.text.toString(),
            zip = binding.editZip.text.toString(),
            city = binding.editCity.text.toString(),
            phoneNumber = binding.editPhone.text.toString(),
            type = when {
                binding.editPhoneTypeHome.isChecked -> PhoneType.HOME
                binding.editPhoneTypeMobile.isChecked -> PhoneType.MOBILE
                binding.editPhoneTypeOffice.isChecked -> PhoneType.OFFICE
                binding.editPhoneTypeFax.isChecked -> PhoneType.FAX
                else -> null
            },
            isDirty = true, // Marquer comme modifié pour la synchronisation
            lastModified = System.currentTimeMillis()
        )
    }

    /**
     * Méthode : newInstance
     * Description : Permet de créer une nouvelle instance du fragment EditFragment.
     * @return Une nouvelle instance d'EditFragment.
     */
    companion object {
        @JvmStatic
        fun newInstance() = EditFragment()
    }
}