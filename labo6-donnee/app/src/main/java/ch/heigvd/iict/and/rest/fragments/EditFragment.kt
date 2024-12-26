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
import ch.heigvd.iict.and.rest.database.converters.CalendarConverter
import ch.heigvd.iict.and.rest.databinding.FragmentEditBinding
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
        // Listener pour le champ Birthday
/*        binding.editBirthday.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePicker = DatePickerDialog(requireContext(), { _, y, m, d ->
                // Formatage et affichage de la date sélectionnée
                val date = String.format("%02d/%02d/%d", d, m + 1, y)
                binding.editBirthday.setText(date) // Met à jour le champ
            }, year, month, day)

            datePicker.show() // Affiche la boîte de dialogue
        }*/

        // Listener pour le bouton Create
        binding.editCreate.setOnClickListener {
            saveContact() // Appelle la fonction pour sauvegarder
        }

        // Listener pour le bouton Cancel
        binding.editCancel.setOnClickListener {
            parentFragmentManager.popBackStack() // Retour en arrière
        }
    }

    private fun saveContact() {

        if (binding.editName.text.toString().isBlank()) {
            Toast.makeText(requireContext(), "Veuillez remplir les champs obligatoires.", Toast.LENGTH_SHORT).show()
            return
        }


  /*      val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        // Parse la chaîne en Date puis obtient le timestamp
        val timestamp = dateFormat.parse(binding.editBirthday.text.toString())?.time
        ?: throw IllegalArgumentException("Date invalide")
        val calendarConverter = CalendarConverter()*/

        // Récupère le type de téléphone sélectionné
        val phoneType = when {
            binding.editPhoneTypeHome.isChecked -> PhoneType.HOME
            binding.editPhoneTypeMobile.isChecked -> PhoneType.MOBILE
            binding.editPhoneTypeOffice.isChecked -> PhoneType.OFFICE
            binding.editPhoneTypeFax.isChecked -> PhoneType.FAX
            else -> null
        }

        // Crée un objet Contact à partir des données du formulaire
        val contact = ch.heigvd.iict.and.rest.models.Contact(
            id = 0, // ID pour un nouveau contact TODO on devrait mettre null
            name = binding.editName.text.toString(),
            firstname = binding.editFirstname.text.toString(),
            email = binding.editEmail.text.toString(),
            birthday = null, // Utilisation du timestamp
            address = binding.editAddress.text.toString(),
            zip = binding.editZip.text.toString(),
            city = binding.editCity.text.toString(),
            phoneNumber = binding.editPhone.text.toString(),
            type = phoneType // À compléter plus tard
        )

        // Sauvegarde dans la base via ViewModel
        contactsViewModel.saveContact(contact)

        // **Actualisation immédiate de la liste des contacts**
        //contactsViewModel.refresh()

        // Message de confirmation
        Toast.makeText(requireContext(), "Contact enregistré !", Toast.LENGTH_SHORT).show()

        // Retour à la liste des contacts
        parentFragmentManager.popBackStack()
    }


    companion object {
        @JvmStatic
        fun newInstance() = EditFragment()
    }

}