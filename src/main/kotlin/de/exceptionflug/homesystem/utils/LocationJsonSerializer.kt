package de.exceptionflug.homesystem.utils

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import org.bukkit.Location
import java.lang.reflect.Type

class LocationJsonSerializer : JsonSerializer<Location> {

    override fun serialize(src: Location?, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement {
        if(src == null) throw IllegalStateException("Cannot serialize a null location")
        val obj = JsonObject()
        obj.addProperty("x", src.x)
        obj.addProperty("y", src.y)
        obj.addProperty("z", src.z)
        obj.addProperty("yaw", src.yaw)
        obj.addProperty("pitch", src.pitch)
        obj.addProperty("world", src.world.name)
        return obj
    }

}