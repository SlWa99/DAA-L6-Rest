package ch.heigvd.iict.and.rest.fragments


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import ch.heigvd.iict.and.rest.databinding.FragmentEditBinding
import ch.heigvd.iict.and.rest.models.Contact
import ch.heigvd.iict.and.rest.viewmodels.ContactsViewModel

class EditFragment : Fragment() {

    private var _binding: FragmentEditBinding? = null
    private val binding get() = _binding!!

    private val contactsViewModel: ContactsViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Observer le contact sélectionné
        contactsViewModel.selectedContact.observe(viewLifecycleOwner) { contact ->
            if (contact != null) {
                binding.editName.setText(contact.name)
                binding.editFirstname.setText(contact.firstname)
                binding.editPhone.setText(contact.phoneNumber)
                binding.editEmail.setText(contact.email)
                binding.editAddress.setText(contact.address)
            }
        }

        // Enregistrement du contact
        binding.btnSave.setOnClickListener {
            val newContact = Contact(
                id = contactsViewModel.selectedContact.value?.id ?: 0,
                name = binding.editName.text.toString(),
                firstname = binding.editFirstname.text.toString(),
                phoneNumber = binding.editPhone.text.toString(),
                email = binding.editEmail.text.toString(),
                address = binding.editAddress.text.toString()
            )
            contactsViewModel.saveContact(newContact)
            parentFragmentManager.popBackStack()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}