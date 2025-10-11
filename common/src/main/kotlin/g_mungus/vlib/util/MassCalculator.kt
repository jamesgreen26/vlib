package g_mungus.vlib.util

import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import g_mungus.vlib.VLib
import g_mungus.vlib.mixin.massCalculator.BitsetDiscreteVoxelShapeAccessor
import net.minecraft.core.BlockPos
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.level.Level
import net.minecraft.world.phys.shapes.CollisionContext
import org.valkyrienskies.mod.common.config.MassDatapackResolver
import java.io.File
import java.io.FileOutputStream
import java.io.FileWriter
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import kotlin.math.max
import kotlin.math.roundToInt


const val DEFAULT_DENSITY = 1000.0
const val MIN_MASS = 10.0

fun calculateMasses(level: Level): String {
    val keys = BuiltInRegistries.BLOCK.keySet().filterNot { it.namespace == "minecraft" }

    val output: MutableMap<ResourceLocation, Double> = mutableMapOf()

    keys.forEach {
        try {
            val block = BuiltInRegistries.BLOCK.get(it)
            val voxelShape =
                block.getShape(block.defaultBlockState(), level, BlockPos(0, 0, 0), CollisionContext.empty())

            val shape = (voxelShape as CanAccessShape).`vlib$getShape`()
            var volume = 0.0

            val existingMass = MassDatapackResolver.getBlockStateMass(block.defaultBlockState())

            BitsetDiscreteVoxelShapeAccessor.forAllBoxes(
                shape,
                { a, b, c, d, e, f ->
                    val xSize = (d - a) / shape.xSize.toDouble()
                    val ySize = (e - b) / shape.ySize.toDouble()
                    val zSize = (f - c) / shape.zSize.toDouble()
                    volume += xSize * ySize * zSize
                }, false
            )

            if (existingMass == null) {
                val newMass = (volume * DEFAULT_DENSITY * 10.0).roundToInt() / 10.0
                output[it] = max(newMass, MIN_MASS)
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    val outputDir = File("mods/vlib_mass_synthetic_mod")
    if (outputDir.exists()) {
        outputDir.deleteRecursively()
    }
    outputDir.mkdirs()

    try {
        val templateFiles = listOf(
            "pack.mcmeta",
            "fabric.mod.json",
            "META-INF/mods.toml",
            "META-INF/MANIFEST.MF",
            "META-INF/neoforge.mods.toml",
            "data/valkyrienskies/vs_mass/vlib_calculated_masses.json"
        )

        templateFiles.forEach { filePath ->
            val resourceStream = VLib::class.java.getResourceAsStream("/mass_mod_template/$filePath")
            if (resourceStream != null) {
                val outputFile = File(outputDir, filePath)
                outputFile.parentFile.mkdirs()

                resourceStream.use { input ->
                    outputFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
            } else {
                VLib.LOGGER.warn("Template file not found: $filePath")
            }
        }
    } catch (e: Exception) {
        VLib.LOGGER.error("Error copying template files", e)
        throw e
    }

    // Build JSON array
    val jsonArray = JsonArray()
    output.forEach { (resourceLocation, mass) ->
        val entry = JsonObject()
        entry.addProperty("block", resourceLocation.toString())
        entry.addProperty("mass", mass)
        jsonArray.add(entry)
    }

    val gson = GsonBuilder().setPrettyPrinting().create()

    val massFile = File(outputDir, "data/valkyrienskies/vs_mass/vlib_calculated_masses.json")
    massFile.parentFile.mkdirs()

    FileWriter(massFile).use { writer ->
        gson.toJson(jsonArray, writer)
    }


    val zipFile = File("mods/vlib_mass_synthetic_mod.jar")
    if (zipFile.exists()) {
        zipFile.delete()
    }

    try {
        ZipOutputStream(FileOutputStream(zipFile)).use { zipOut ->
            zipOut.setMethod(ZipOutputStream.DEFLATED)
            zipOut.setLevel(-1)
            zipDirectory(outputDir, "", zipOut)
        }

        if (outputDir.exists()) {
            outputDir.deleteRecursively()
        }
    } catch (e: Exception) {
        VLib.LOGGER.error("Error creating jar file", e)
        throw e
    }

    return zipFile.absolutePath
}

private fun zipDirectory(sourceDir: File, parentPath: String, zipOut: ZipOutputStream) {
    sourceDir.listFiles()?.forEach { file ->
        if (file.isDirectory) {
            val dirPath = if (parentPath.isEmpty()) {
                "${file.name}/"
            } else {
                "$parentPath/${file.name}/"
            }
            zipOut.putNextEntry(ZipEntry(dirPath))
            zipOut.closeEntry()
            zipDirectory(file, if (parentPath.isEmpty()) file.name else "$parentPath/${file.name}", zipOut)
        } else {
            val filePath = if (parentPath.isEmpty()) {
                file.name
            } else {
                "$parentPath/${file.name}"
            }
            zipOut.putNextEntry(ZipEntry(filePath))
            file.inputStream().use { input ->
                input.copyTo(zipOut)
            }
            zipOut.closeEntry()
        }
    }
}