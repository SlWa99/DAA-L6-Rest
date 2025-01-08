/**
 * Nom du fichier : Contact.kt
 * Description    : Définition de l'entité `Contact`, utilisée pour représenter les contacts dans
 *                  la base de données locale. Contient des informations personnelles et des
 *                  métadonnées pour la synchronisation avec le serveur.
 * Auteur         : ICI
 * Date           : 08 janvier 2025
 */
package ch.heigvd.iict.and.rest.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

/**
 * Classe : Contact
 * Description : Représente un contact stocké dans la base de données locale. Inclut des
 *               champs pour les détails personnels et des métadonnées pour le suivi
 *               des modifications et la synchronisation avec un serveur.
 * @property id Identifiant unique auto-généré pour le contact dans la base locale.
 * @property uuid Identifiant unique côté serveur, pour lier le contact à la base distante.
 * @property name Nom de famille du contact (obligatoire).
 * @property firstname Prénom du contact.
 * @property birthday Date de naissance au format `Calendar`.
 * @property email Adresse e-mail du contact.
 * @property address Adresse postale.
 * @property zip Code postal de l'adresse.
 * @property city Ville de l'adresse.
 * @property type Type de numéro de téléphone (maison, bureau, mobile, fax).
 * @property phoneNumber Numéro de téléphone associé.
 * @property isDirty Indique si le contact a des modifications à synchroniser avec le serveur.
 * @property lastModified Timestamp de la dernière modification apportée au contact.
 */
@Entity
data class Contact(@PrimaryKey(autoGenerate = true)
                   var id: Long? = null,
                   var uuid: String? = null, // Identifiant unique serveur
                   var name: String,
                   var firstname: String?,
                   var birthday : Calendar?,
                   var email: String?,
                   var address: String?,
                   var zip: String?,
                   var city: String?,
                   var type: PhoneType?,
                   var phoneNumber: String?,
                   var isDirty: Boolean = false, // Synchronisation nécessaire ?
                   var lastModified: Long = System.currentTimeMillis() // Date dernière modification
)