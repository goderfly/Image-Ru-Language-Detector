package utils

import java.io.File
import kotlin.reflect.KProperty

class StorableString<in R>(
    private val defaultValue: String = " "
) {
    operator fun getValue(thisRef: R, property: KProperty<*>): String {
        return runCatching { File("cache/${property.name}").inputStream().bufferedReader().use { it.readText() } }
            .getOrElse { defaultValue }
    }

    operator fun setValue(thisRef: R, property: KProperty<*>, value: String) {
        File("cache").mkdir()
        File("cache/${property.name}").apply { writeText(value) }
    }
}