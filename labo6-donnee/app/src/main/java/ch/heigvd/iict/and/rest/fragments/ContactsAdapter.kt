/**
 * Nom du fichier : ContactsAdapter.kt
 * Description    : Implémente un adapter pour le RecyclerView qui affiche une liste de contacts.
 *                  Utilise DiffUtil pour optimiser la mise à jour des données et gère les clics
 *                  sur les éléments de la liste. Permet également de modifier l'apparence des
 *                  éléments en fonction de leur état.
 * Auteur         : Bugna, Slimani & Steiner
 * Date           : 08 janvier 2025
 */

package ch.heigvd.iict.and.rest.fragments

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView.OnItemClickListener
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import ch.heigvd.iict.and.rest.R
import ch.heigvd.iict.and.rest.models.Contact
import ch.heigvd.iict.and.rest.models.PhoneType

/**
 * Classe : ContactsAdapter
 * Description : Adapter pour un RecyclerView affichant une liste de contacts.
 *               Utilise DiffUtil pour mettre à jour efficacement la liste des contacts.
 * @param contacts Liste initiale des contacts à afficher.
 * @param clickListener Écouteur pour gérer les clics sur les éléments de la liste.
 */
class ContactsAdapter(contacts: List<Contact>, private val clickListener: OnItemClickListener) :
    RecyclerView.Adapter<ContactsAdapter.ViewHolder>() {

    /**
     * Liste des contacts affichés dans le RecyclerView.
     * La mise à jour de cette liste utilise DiffUtil pour optimiser les changements.
     */
    var contacts: List<Contact> = contacts
        set(value) {
            val diffCallBack = ContactsDiffCallBack(contacts, value)
            val diffItem = DiffUtil.calculateDiff(diffCallBack)
            field = value
            diffItem.dispatchUpdatesTo(this)
        }

    /**
     * Crée un ViewHolder pour un élément de la liste.
     * @param parent Le conteneur parent du ViewHolder.
     * @param viewType Le type de vue (non utilisé ici, car un seul type de vue est défini).
     * @return Un ViewHolder pour afficher un contact.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.row_item_contact, parent, false)
        return ViewHolder(view)
    }

    /**
     * Lie un contact au ViewHolder à une position donnée.
     * @param holder Le ViewHolder pour l'élément.
     * @param position La position de l'élément dans la liste.
     */
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val contactToDisplay = contacts[position]
        holder.bind(contactToDisplay, position)

        // Change la couleur du logo en fonction de l'état "dirty"
        val logoColorRes = if (contactToDisplay.isDirty) {
            R.color.orange // Orange pour les contacts "dirty"
        } else {
            R.color.green // Vert pour les contacts synchronisés
        }
        holder.image.setColorFilter(ContextCompat.getColor(holder.image.context, logoColorRes))
    }

    /**
     * Retourne le nombre total d'éléments dans la liste.
     * @return Le nombre de contacts affichés.
     */
    override fun getItemCount() = contacts.size

    /**
     * Retourne le type de vue pour une position donnée (toujours 0 ici, car un seul type est utilisé).
     * @param position La position de l'élément.
     * @return Le type de vue.
     */
    override fun getItemViewType(position: Int) = 0

    /**
     * Classe interne : ViewHolder
     * Description : Représente un élément individuel de la liste dans le RecyclerView.
     * @param view La vue associée à l'élément.
     */
    inner class ViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {

        val image = view.findViewById<ImageView>(R.id.contact_image)
        private val name = view.findViewById<TextView>(R.id.contact_name)
        private val phonenumber = view.findViewById<TextView>(R.id.contact_phonenumber)
        private val type = view.findViewById<ImageView>(R.id.contact_phonenumber_type)

        /**
         * Associe les données d'un contact au ViewHolder.
         * @param contact Le contact à afficher.
         * @param position La position de l'élément dans la liste.
         */
        fun bind(contact: Contact, position: Int) {
            view.setOnClickListener {
                clickListener.onItemClick(null, view, position, contact.id!!)
            }
            name.text = "${contact.name} ${contact.firstname}"
            phonenumber.text = "${contact.phoneNumber}"

            val colRes = android.R.color.holo_green_dark
            image.setColorFilter(ContextCompat.getColor(image.context, colRes))

            when (contact.type) {
                PhoneType.HOME -> type.setImageResource(R.drawable.phone)
                PhoneType.OFFICE -> type.setImageResource(R.drawable.office)
                PhoneType.MOBILE -> type.setImageResource(R.drawable.cellphone)
                PhoneType.FAX -> type.setImageResource(R.drawable.fax)
                else -> type.setImageResource(android.R.color.transparent)
            }

        }
    }
}

/**
 * Classe : ContactsDiffCallBack
 * Description : Compare deux listes de contacts pour identifier les différences
 *               lors de la mise à jour du RecyclerView.
 * @param oldList Liste actuelle des contacts.
 * @param newList Nouvelle liste des contacts.
 */
class ContactsDiffCallBack(private val oldList: List<Contact>, private val newList: List<Contact>) :
    DiffUtil.Callback() {

    /**
     * Retourne la taille de l'ancienne liste.
     * @return La taille de l'ancienne liste.
     */
    override fun getOldListSize() = oldList.size

    /**
     * Retourne la taille de la nouvelle liste.
     * @return La taille de la nouvelle liste.
     */
    override fun getNewListSize() = newList.size

    /**
     * Vérifie si deux éléments représentent le même contact.
     * @param oldItemPosition Position dans l'ancienne liste.
     * @param newItemPosition Position dans la nouvelle liste.
     * @return `true` si les contacts ont le même ID, sinon `false`.
     */
    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].id == newList[newItemPosition].id
    }

    /**
     * Vérifie si le contenu de deux contacts est identique.
     * @param oldItemPosition Position dans l'ancienne liste.
     * @param newItemPosition Position dans la nouvelle liste.
     * @return `true` si les contenus sont identiques, sinon `false`.
     */
    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {

        val oldContact = oldList[oldItemPosition]
        val newContact = newList[newItemPosition]

        return oldContact.name == newContact.name &&
                oldContact.firstname == newContact.firstname &&
                oldContact.birthday == newContact.birthday &&
                oldContact.email == newContact.email &&
                oldContact.address == newContact.address &&
                oldContact.zip == newContact.zip &&
                oldContact.city == newContact.city &&
                oldContact.type == newContact.type &&
                oldContact.phoneNumber == newContact.phoneNumber
    }
}