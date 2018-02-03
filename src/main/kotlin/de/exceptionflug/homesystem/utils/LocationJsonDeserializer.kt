package de.exceptionflug.homesystem.utils

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import org.bukkit.Bukkit
import org.bukkit.Location
import java.lang.reflect.Type

class LocationJsonDeserializer() : JsonDeserializer<Location> {

    override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): Location {
        if(json == null) throw IllegalStateException("Cannot deserialize a null location")
        if(json !is JsonObject) throw IllegalStateException("JsonElement for location deserialization is in a incorrect type")
        return Location(Bukkit.getWorld(json["world"].toString()), json["xCord"].asDouble, json["yCord"].asDouble, json["zCord"].asDouble, json["yaw"].asFloat, json["pitch"].asFloat)
    }

}