/**
 * Nom du fichier : ContactsDao.kt
 * Description    : Interface DAO pour gérer les opérations CRUD sur la table `Contact` dans
 *                  la base de données locale. Inclut des requêtes personnalisées pour des
 *                  opérations spécifiques comme la récupération des contacts marqués "dirty".
 * Auteur         : Bugna, Slimani & Steiner
 * Date           : 08 janvier 2025
 */
package ch.heigvd.iict.and.rest.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import ch.heigvd.iict.and.rest.models.Contact

/**
 * Interface : ContactsDao
 * Description : Fournit les méthodes d'accès aux données (DAO) pour la gestion de la table `Contact`.
 *               Inclut des opérations CRUD standard et des requêtes personnalisées pour des cas d'utilisation spécifiques.
 */
@Dao
interface ContactsDao {

    /**
     * Méthode : insert
     * Description : Insère un nouvel enregistrement dans la table `Contact`.
     * @param contact L'objet `Contact` à insérer.
     * @return L'identifiant unique (ID) de l'enregistrement inséré.
     */
    @Insert
    fun insert(contact: Contact) : Long

    /**
     * Méthode : update
     * Description : Met à jour un enregistrement existant dans la table `Contact`.
     * @param contact L'objet `Contact` avec les nouvelles données.
     */
    @Update
    fun update(contact: Contact)

    /**
     * Méthode : delete
     * Description : Supprime un enregistrement de la table `Contact`.
     * @param contact L'objet `Contact` à supprimer.
     */
    @Delete
    fun delete(contact: Contact)

    /**
     * Méthode : getAllContactsLiveData
     * Description : Récupère tous les contacts sous forme de `LiveData` d'une liste.
     * @return Un objet `LiveData` contenant une liste de tous les contacts.
     */
    @Query("SELECT * FROM Contact")
    fun getAllContactsLiveData() : LiveData<List<Contact>>

    /**
     * Méthode : getAllContacts
     * Description : Récupère tous les contacts sous forme de liste.
     * @return Une liste contenant tous les contacts dans la table.
     */
    @Query("SELECT * FROM Contact")
    fun getAllContacts() : List<Contact>

    /**
     * Méthode : getContactById
     * Description : Récupère un contact spécifique par son identifiant unique.
     * @param id L'identifiant du contact recherché.
     * @return Un objet `LiveData` contenant le contact correspondant.
     */
    @Query("SELECT * FROM Contact WHERE id = :id")
    fun getContactById(id: Long): LiveData<Contact>

    /**
     * Méthode : getCount
     * Description : Récupère le nombre total d'enregistrements dans la table `Contact`.
     * @return Le nombre total de contacts dans la table.
     */
    @Query("SELECT COUNT(*) FROM Contact")
    fun getCount() : Int

    /**
     * Méthode : clearAllContacts
     * Description : Supprime tous les enregistrements dans la table `Contact`.
     */
    @Query("DELETE FROM Contact")
    fun clearAllContacts()

    /**
     * Méthode : resetPrimaryKey
     * Description : Réinitialise l'autoincrémentation de la clé primaire pour la table `Contact`.
     *               Cette méthode est asynchrone et doit être appelée dans une coroutine.
     */
    @Query("DELETE FROM sqlite_sequence WHERE name='Contact'")
    suspend fun resetPrimaryKey()

    /**
     * Méthode : getDirtyContacts
     * Description : Récupère les contacts marqués comme "dirty" (modifiés ou non synchronisés),
     *               triés par date de dernière modification ascendante.
     * @return Une liste de contacts marqués "dirty".
     */
    @Query("SELECT * FROM Contact WHERE isDirty = 1 ORDER BY lastModified ASC")
    fun getDirtyContacts(): List<Contact>
}