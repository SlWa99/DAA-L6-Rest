package ch.heigvd.iict.and.rest.database.converters

import com.google.gson.*
import java.lang.reflect.Type
import java.text.SimpleDateFormat
import java.util.*

class CalendarJsonAdapter : JsonSerializer<Calendar>, JsonDeserializer<Calendar> {
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    override fun serialize(src: Calendar?, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement {
        return if (src == null) JsonNull.INSTANCE else JsonPrimitive(dateFormat.format(src.time))
    }

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