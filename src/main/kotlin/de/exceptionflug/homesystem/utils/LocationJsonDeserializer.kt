package de.exceptionflug.homesystem.utils

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import org.bukkit.Bukkit
import org.bukkit.Location
import java.lang.reflect.Type

class LocationJsonDeserializer : JsonDeserializer<Location?> {

    override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): Location? {
        if(json == null) throw IllegalStateException("Cannot deserialize a null location")
        if(json !is JsonObject) throw IllegalStateException("JsonElement for location deserialization is in an incorrect type")
        val world = Bukkit.getWorld(json["world"].asString)
        if(world == null) {
            Bukkit.getLogger().warning("[HomeSystem] Cannot load serialized location $json. World is null.")
            return null
        }
        return Location(world, json["x"].asDouble, json["y"].asDouble, json["z"].asDouble, json["yaw"].asFloat, json["pitch"].asFloat)
    }

}