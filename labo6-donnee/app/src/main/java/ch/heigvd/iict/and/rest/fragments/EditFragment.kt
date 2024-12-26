package ch.heigvd.iict.and.rest.fragments

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import ch.heigvd.iict.and.rest.ContactsApplication
import ch.heigvd.iict.and.rest.R
import ch.heigvd.iict.and.rest.database.converters.CalendarConverter
import ch.heigvd.iict.and.rest.databinding.FragmentEditBinding
import ch.heigvd.iict.and.rest.models.Contact
import ch.heigvd.iict.and.rest.models.PhoneType
import ch.heigvd.iict.and.rest.viewmodels.ContactsViewModel
import ch.heigvd.iict.and.rest.viewmodels.ContactsViewModelFactory
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class EditFragment : Fragment() {

    private lateinit var binding: FragmentEditBinding
    private val contactsViewModel: ContactsViewModel by activityViewModels {
        ContactsViewModelFactory((requireActivity().application as ContactsApplication).repository)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentEditBinding.inflate(inflater, container, false)
        return binding.root
    }

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


    private fun setupButtons() {
        // Bouton Create (nouveau contact)
        binding.editCreate.setOnClickListener {
            val newContact = createContactFromInputs()
            contactsViewModel.saveContact(newContact)
            parentFragmentManager.popBackStack()
        }

        // Bouton Save (modification)
        binding.editSave.setOnClickListener {
            val updatedContact = createContactFromInputs().copy(
                id = contactsViewModel.selectedContact.value?.id
            )
            contactsViewModel.saveContact(updatedContact)
            parentFragmentManager.popBackStack()
        }

        // Bouton Delete
        binding.editDelete.setOnClickListener {
            contactsViewModel.selectedContact.value?.let { contact ->
                contactsViewModel.deleteContact(contact)
                parentFragmentManager.popBackStack()
            }
        }

        // Bouton Cancel (commun aux deux modes)
        binding.editCancel.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }


    private fun setupCreateMode() {
        binding.editTitle.text = getString(R.string.fragment_detail_title_new)
        // Afficher uniquement Create et Cancel
        binding.editCreate.visibility = View.VISIBLE
        binding.editSave.visibility = View.GONE
        binding.editDelete.visibility = View.GONE
    }

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

    private fun createContactFromInputs(): Contact {
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
            }
        )
    }


    companion object {
        @JvmStatic
        fun newInstance() = EditFragment()
    }

}