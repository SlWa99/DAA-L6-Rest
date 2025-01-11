/**
 * Nom du fichier : CalendarConverter.kt
 * Description    : Fournit des méthodes pour convertir des objets `Calendar` en types compatibles
 *                  avec Room (et vice-versa). Utilisé pour stocker des dates dans la base de données.
 * Auteur         : Bugna, Slimani & Steiner
 * Date           : 08 janvier 2025
 */

package ch.heigvd.iict.and.rest.database.converters

import androidx.room.TypeConverter
import java.util.*


/**
 * Classe : CalendarConverter
 * Description : Fournit des convertisseurs pour permettre à Room de manipuler des objets `Calendar`.
 *               Les objets `Calendar` sont convertis en `Long` pour être stockés dans la base de données,
 *               et reconvertis en `Calendar` lors de la récupération.
 */
class CalendarConverter {
    /**
     * Méthode : toCalendar
     * Description : Convertit un timestamp en millisecondes (représenté par un `Long`) en objet `Calendar`.
     * @param dateLong Le timestamp à convertir en `Calendar`.
     * @return Un objet `Calendar` initialisé avec la date correspondant au timestamp donné.
     */
    @TypeConverter
    fun toCalendar(dateLong: Long) =
        Calendar.getInstance().apply {
            time = Date(dateLong)
        }

    /**
     * Méthode : fromCalendar
     * Description : Convertit un objet `Calendar` en un timestamp en millisecondes (`Long`)
     *               pour le stockage dans la base de données.
     * @param date L'objet `Calendar` à convertir.
     * @return Un `Long` représentant le timestamp correspondant à la date dans le `Calendar`.
     */
    @TypeConverter
    fun fromCalendar(date: Calendar) =
        date.time.time
}