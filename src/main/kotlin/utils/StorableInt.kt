package utils

import java.io.File
import kotlin.reflect.KProperty

class StorableInt<in R> {
    operator fun getValue(thisRef: R, property: KProperty<*>): Int {
        return runCatching { File("cache/${property.name}").inputStream().bufferedReader().use { it.readText() } }
            .getOrElse { "0" }.toInt()
    }

    operator fun setValue(thisRef: R, property: KProperty<*>, value: Int) {
        File("cache").mkdir()
        File("cache/${property.name}").apply { writeText(value.toString()) }
    }
}