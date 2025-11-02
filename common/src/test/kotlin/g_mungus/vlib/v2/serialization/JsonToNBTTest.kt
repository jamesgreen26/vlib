package g_mungus.vlib.v2.serialization

import g_mungus.vlib.v2.util.serialization.toJson
import g_mungus.vlib.v2.util.serialization.toTag
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import net.minecraft.nbt.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested

class JsonToNBTTest {

    @Nested
    @DisplayName("JsonElement.toTag() tests")
    inner class JsonToTagTests {

        @Test
        fun `should convert JsonPrimitive boolean to ByteTag`() {
            val jsonTrue = JsonPrimitive(true)
            val jsonFalse = JsonPrimitive(false)

            val tagTrue = jsonTrue.toTag()
            val tagFalse = jsonFalse.toTag()

            assertTrue(tagTrue is ByteTag)
            assertEquals(1.toByte(), (tagTrue as ByteTag).asByte)

            assertTrue(tagFalse is ByteTag)
            assertEquals(0.toByte(), (tagFalse as ByteTag).asByte)
        }

        @Test
        fun `should convert JsonPrimitive int to IntTag`() {
            val json = JsonPrimitive(42)
            val tag = json.toTag()

            assertTrue(tag is IntTag)
            assertEquals(42, (tag as IntTag).asInt)
        }

        @Test
        fun `should convert JsonPrimitive long to LongTag`() {
            val json = JsonPrimitive(9876543210L)
            val tag = json.toTag()

            assertTrue(tag is LongTag)
            assertEquals(9876543210L, (tag as LongTag).asLong)
        }

        @Test
        fun `should convert JsonPrimitive double to DoubleTag`() {
            val json = JsonPrimitive(3.14159)
            val tag = json.toTag()

            assertTrue(tag is DoubleTag)
            assertEquals(3.14159, (tag as DoubleTag).asDouble, 0.00001)
        }

        @Test
        fun `should convert JsonPrimitive string to StringTag`() {
            val json = JsonPrimitive("hello world")
            val tag = json.toTag()

            assertTrue(tag is StringTag)
            assertEquals("hello world", (tag as StringTag).asString)
        }

        @Test
        fun `should convert JsonArray to ListTag`() {
            val json = buildJsonArray {
                add(JsonPrimitive(1))
                add(JsonPrimitive(2))
                add(JsonPrimitive(3))
            }

            val tag = json.toTag()

            assertTrue(tag is ListTag)
            val listTag = tag as ListTag
            assertEquals(3, listTag.size)
            assertEquals(1, (listTag[0] as IntTag).asInt)
            assertEquals(2, (listTag[1] as IntTag).asInt)
            assertEquals(3, (listTag[2] as IntTag).asInt)
        }

        @Test
        fun `should convert JsonObject to CompoundTag`() {
            val json = buildJsonObject {
                put("name", JsonPrimitive("test"))
                put("value", JsonPrimitive(42))
                put("enabled", JsonPrimitive(true))
            }

            val tag = json.toTag()

            assertTrue(tag is CompoundTag)
            val compound = tag as CompoundTag
            assertEquals("test", compound.getString("name"))
            assertEquals(42, compound.getInt("value"))
            assertEquals(1.toByte(), compound.getByte("enabled"))
        }

        @Test
        fun `should convert nested JsonObject to nested CompoundTag`() {
            val json = buildJsonObject {
                put("outer", buildJsonObject {
                    put("inner", JsonPrimitive("value"))
                    put("number", JsonPrimitive(123))
                })
            }

            val tag = json.toTag()

            assertTrue(tag is CompoundTag)
            val compound = tag as CompoundTag
            val innerCompound = compound.getCompound("outer")
            assertEquals("value", innerCompound.getString("inner"))
            assertEquals(123, innerCompound.getInt("number"))
        }

        @Test
        fun `should convert nested JsonArray to nested ListTag`() {
            val json = buildJsonArray {
                add(buildJsonArray {
                    add(JsonPrimitive(1))
                    add(JsonPrimitive(2))
                })
                add(buildJsonArray {
                    add(JsonPrimitive(3))
                    add(JsonPrimitive(4))
                })
            }

            val tag = json.toTag()

            assertTrue(tag is ListTag)
            val listTag = tag as ListTag
            assertEquals(2, listTag.size)

            val firstList = listTag[0] as ListTag
            assertEquals(1, (firstList[0] as IntTag).asInt)
            assertEquals(2, (firstList[1] as IntTag).asInt)

            val secondList = listTag[1] as ListTag
            assertEquals(3, (secondList[0] as IntTag).asInt)
            assertEquals(4, (secondList[1] as IntTag).asInt)
        }

        @Test
        fun `should handle empty JsonObject`() {
            val json = buildJsonObject { }
            val tag = json.toTag()

            assertTrue(tag is CompoundTag)
            assertEquals(0, (tag as CompoundTag).allKeys.size)
        }

        @Test
        fun `should handle empty JsonArray`() {
            val json = buildJsonArray { }
            val tag = json.toTag()

            assertTrue(tag is ListTag)
            assertEquals(0, (tag as ListTag).size)
        }
    }

    @Nested
    @DisplayName("Tag.toJson() tests")
    inner class TagToJsonTests {

        @Test
        fun `should convert ByteTag to JsonPrimitive boolean`() {
            val tagTrue = ByteTag.valueOf(true)
            val tagFalse = ByteTag.valueOf(false)

            val jsonTrue = tagTrue.toJson()
            val jsonFalse = tagFalse.toJson()

            assertTrue(jsonTrue is JsonPrimitive)
            assertEquals(true, (jsonTrue as JsonPrimitive).content.toBoolean())

            assertTrue(jsonFalse is JsonPrimitive)
            assertEquals(false, (jsonFalse as JsonPrimitive).content.toBoolean())
        }

        @Test
        fun `should convert IntTag to JsonPrimitive`() {
            val tag = IntTag.valueOf(42)
            val json = tag.toJson()

            assertTrue(json is JsonPrimitive)
            assertEquals("42", (json as JsonPrimitive).content)
        }

        @Test
        fun `should convert LongTag to JsonPrimitive`() {
            val tag = LongTag.valueOf(9876543210L)
            val json = tag.toJson()

            assertTrue(json is JsonPrimitive)
            assertEquals("9876543210", (json as JsonPrimitive).content)
        }

        @Test
        fun `should convert DoubleTag to JsonPrimitive`() {
            val tag = DoubleTag.valueOf(3.14159)
            val json = tag.toJson()

            assertTrue(json is JsonPrimitive)
            assertEquals(3.14159, (json as JsonPrimitive).content.toDouble(), 0.00001)
        }

        @Test
        fun `should convert StringTag to JsonPrimitive`() {
            val tag = StringTag.valueOf("hello world")
            val json = tag.toJson()

            assertTrue(json is JsonPrimitive)
            assertEquals("hello world", (json as JsonPrimitive).content)
        }

        @Test
        fun `should convert ListTag to JsonArray`() {
            val tag = ListTag().apply {
                add(IntTag.valueOf(1))
                add(IntTag.valueOf(2))
                add(IntTag.valueOf(3))
            }

            val json = tag.toJson()

            assertTrue(json is JsonArray)
            val array = json as JsonArray
            assertEquals(3, array.size)
            assertEquals("1", (array[0] as JsonPrimitive).content)
            assertEquals("2", (array[1] as JsonPrimitive).content)
            assertEquals("3", (array[2] as JsonPrimitive).content)
        }

        @Test
        fun `should convert CompoundTag to JsonObject`() {
            val tag = CompoundTag().apply {
                putString("name", "test")
                putInt("value", 42)
                putBoolean("enabled", true)
            }

            val json = tag.toJson()

            assertTrue(json is JsonObject)
            val obj = json as JsonObject
            assertEquals("test", (obj["name"] as JsonPrimitive).content)
            assertEquals("42", (obj["value"] as JsonPrimitive).content)
            assertEquals("true", (obj["enabled"] as JsonPrimitive).content)
        }

        @Test
        fun `should convert nested CompoundTag to nested JsonObject`() {
            val tag = CompoundTag().apply {
                put("outer", CompoundTag().apply {
                    putString("inner", "value")
                    putInt("number", 123)
                })
            }

            val json = tag.toJson()

            assertTrue(json is JsonObject)
            val obj = json as JsonObject
            val innerObj = obj["outer"] as JsonObject
            assertEquals("value", (innerObj["inner"] as JsonPrimitive).content)
            assertEquals("123", (innerObj["number"] as JsonPrimitive).content)
        }

        @Test
        fun `should convert nested ListTag to nested JsonArray`() {
            val tag = ListTag().apply {
                add(ListTag().apply {
                    add(IntTag.valueOf(1))
                    add(IntTag.valueOf(2))
                })
                add(ListTag().apply {
                    add(IntTag.valueOf(3))
                    add(IntTag.valueOf(4))
                })
            }

            val json = tag.toJson()

            assertTrue(json is JsonArray)
            val array = json as JsonArray
            assertEquals(2, array.size)

            val firstArray = array[0] as JsonArray
            assertEquals("1", (firstArray[0] as JsonPrimitive).content)
            assertEquals("2", (firstArray[1] as JsonPrimitive).content)

            val secondArray = array[1] as JsonArray
            assertEquals("3", (secondArray[0] as JsonPrimitive).content)
            assertEquals("4", (secondArray[1] as JsonPrimitive).content)
        }

        @Test
        fun `should handle empty CompoundTag`() {
            val tag = CompoundTag()
            val json = tag.toJson()

            assertTrue(json is JsonObject)
            assertEquals(0, (json as JsonObject).size)
        }

        @Test
        fun `should handle empty ListTag`() {
            val tag = ListTag()
            val json = tag.toJson()

            assertTrue(json is JsonArray)
            assertEquals(0, (json as JsonArray).size)
        }

        @Test
        fun `should convert ShortTag to JsonPrimitive`() {
            val tag = ShortTag.valueOf(100)
            val json = tag.toJson()

            assertTrue(json is JsonPrimitive)
            assertEquals("100", (json as JsonPrimitive).content)
        }

        @Test
        fun `should convert FloatTag to JsonPrimitive`() {
            val tag = FloatTag.valueOf(2.5f)
            val json = tag.toJson()

            assertTrue(json is JsonPrimitive)
            assertEquals(2.5f, (json as JsonPrimitive).content.toFloat(), 0.0001f)
        }
    }

    @Nested
    @DisplayName("Round-trip conversion tests")
    inner class RoundTripTests {

        @Test
        fun `should round-trip simple JsonObject`() {
            val originalJson = buildJsonObject {
                put("name", JsonPrimitive("test"))
                put("value", JsonPrimitive(42))
                put("enabled", JsonPrimitive(true))
                put("pi", JsonPrimitive(3.14))
            }

            val tag = originalJson.toTag()
            val resultJson = tag.toJson()

            assertTrue(resultJson is JsonObject)
            val obj = resultJson as JsonObject
            assertEquals("test", (obj["name"] as JsonPrimitive).content)
            assertEquals("42", (obj["value"] as JsonPrimitive).content)
            assertEquals("true", (obj["enabled"] as JsonPrimitive).content)
            assertEquals("3.14", (obj["pi"] as JsonPrimitive).content)
        }

        @Test
        fun `should round-trip simple JsonArray`() {
            val originalJson = buildJsonArray {
                add(JsonPrimitive(1))
                add(JsonPrimitive(2))
                add(JsonPrimitive(3))
            }

            val tag = originalJson.toTag()
            val resultJson = tag.toJson()

            assertTrue(resultJson is JsonArray)
            val array = resultJson as JsonArray
            assertEquals(3, array.size)
            assertEquals("1", (array[0] as JsonPrimitive).content)
            assertEquals("2", (array[1] as JsonPrimitive).content)
            assertEquals("3", (array[2] as JsonPrimitive).content)
        }

        @Test
        fun `should round-trip nested structure`() {
            val originalJson = buildJsonObject {
                put("player", buildJsonObject {
                    put("name", JsonPrimitive("Steve"))
                    put("health", JsonPrimitive(20))
                    put("inventory", buildJsonArray {
                        add(JsonPrimitive("diamond_sword"))
                        add(JsonPrimitive("golden_apple"))
                    })
                })
                put("world", buildJsonObject {
                    put("seed", JsonPrimitive(123456789L))
                    put("time", JsonPrimitive(1000))
                })
            }

            val tag = originalJson.toTag()
            val resultJson = tag.toJson()

            assertTrue(resultJson is JsonObject)
            val obj = resultJson as JsonObject

            val player = obj["player"] as JsonObject
            assertEquals("Steve", (player["name"] as JsonPrimitive).content)
            assertEquals("20", (player["health"] as JsonPrimitive).content)

            val inventory = player["inventory"] as JsonArray
            assertEquals(2, inventory.size)
            assertEquals("diamond_sword", (inventory[0] as JsonPrimitive).content)
            assertEquals("golden_apple", (inventory[1] as JsonPrimitive).content)

            val world = obj["world"] as JsonObject
            assertEquals("123456789", (world["seed"] as JsonPrimitive).content)
            assertEquals("1000", (world["time"] as JsonPrimitive).content)
        }
    }
}
