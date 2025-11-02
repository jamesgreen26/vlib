package g_mungus.vlib.v2.util.serialization

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.double
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.int
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.long
import kotlinx.serialization.json.longOrNull
import net.minecraft.nbt.*
import kotlin.collections.iterator

fun JsonElement.toTag(): Tag = when (this) {
    is JsonObject -> CompoundTag().apply {
        for ((key, value) in this@toTag) put(key, value.toTag())
    }
    is JsonArray -> ListTag().apply {
        for (e in this@toTag) add(e.toTag())
    }
    is JsonPrimitive -> when {
        booleanOrNull != null -> ByteTag.valueOf(boolean)
        intOrNull != null -> IntTag.valueOf(int)
        longOrNull != null -> LongTag.valueOf(long)
        doubleOrNull != null -> DoubleTag.valueOf(double)
        else -> StringTag.valueOf(content)
    }
}

fun Tag.toJson(): JsonElement = when (this) {
    is CompoundTag -> buildJsonObject {
        for (key in this@toJson.allKeys) put(key, this@toJson.get(key)!!.toJson())
    }
    is ListTag -> buildJsonArray {
        for (tag in this@toJson) add(tag.toJson())
    }
    is ByteTag -> JsonPrimitive(asByte != 0.toByte())
    is NumericTag -> JsonPrimitive(asNumber)
    is StringTag -> JsonPrimitive(asString)
    else -> JsonPrimitive(this.toString())
}