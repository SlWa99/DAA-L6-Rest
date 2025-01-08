/**
 * Nom du fichier : CalendarJsonAdapter.kt
 * Description    : Fournit un adaptateur pour sérialiser et désérialiser des objets `Calendar`
 *                  en JSON, en utilisant le format de date `yyyy-MM-dd`. Compatible avec Gson.
 * Auteur         : ICI
 * Date           : 08 janvier 2025
 */
package ch.heigvd.iict.and.rest.database.converters

import com.google.gson.*
import java.lang.reflect.Type
import java.text.SimpleDateFormat
import java.util.*

/**
 * Classe : CalendarJsonAdapter
 * Description : Implémente un adaptateur pour convertir un objet `Calendar` en JSON
 *               (et vice-versa) via la bibliothèque Gson. Utilise le format ISO standard
 *               "yyyy-MM-dd" pour la conversion des dates.
 */
class CalendarJsonAdapter : JsonSerializer<Calendar>, JsonDeserializer<Calendar> {
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    /**
     * Méthode : serialize
     * Description : Convertit un objet `Calendar` en élément JSON. Retourne `JsonNull` si
     *               l'objet source est null.
     * @param src L'objet `Calendar` à sérialiser.
     * @param typeOfSrc Le type de l'objet source.
     * @param context Contexte de sérialisation fourni par Gson.
     * @return Un `JsonElement` représentant la date formatée.
     */
    override fun serialize(src: Calendar?, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement {
        return if (src == null) JsonNull.INSTANCE else JsonPrimitive(dateFormat.format(src.time))
    }

    /**
     * Méthode : deserialize
     * Description : Convertit un élément JSON en objet `Calendar`. Si le JSON est invalide ou null,
     *               retourne `null`.
     * @param json L'élément JSON à convertir.
     * @param typeOfT Le type cible pour la désérialisation.
     * @param context Contexte de désérialisation fourni par Gson.
     * @return Un objet `Calendar` ou null si le JSON est invalide.
     */
    override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): Calendar? {
        if (json == null || json.isJsonNull) return null

        return try {
            val date = dateFormat.parse(json.asString)
            Calendar.getInstance().apply {
                time = date ?: Date()
            }
        } catch (e: Exception) {
            null
        }
    }
}