package de.exceptionflug.homesystem.storage

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import de.exceptionflug.homesystem.home.Home
import de.exceptionflug.homesystem.utils.LocationJsonSerializer
import org.bukkit.Location
import java.io.*
import java.util.*
import kotlin.collections.HashSet

class JsonHomeStorage(val backend: File) : IHomeStorage {

    companion object {
        val gson: Gson = GsonBuilder().registerTypeAdapter(Location::class.java, LocationJsonSerializer()).setPrettyPrinting().create()
    }

    private var homes: MutableSet<Home> = HashSet()

    init {
        if(!backend.exists()) {
            backend.createNewFile()
        } else if(backend.isDirectory) throw IllegalStateException("The given file is a directory!")
        JsonReader(BufferedReader(FileReader(backend))).use {
            this.homes = gson.fromJson<JsonHomeStorage>(it, this::class.java).homes
        }
    }

    override fun save(home: Home) {
        if(!homes.contains(home)) {
            homes.add(home)
        }
        gson.toJson(this, this::class.java, JsonWriter(BufferedWriter(FileWriter(backend))))
    }

    override fun load(id: UUID): Home? {
        for(home in homes) {
            if(home.id == id) return home
        }
        // If the desired home is not in memory, try to reload from the backend
        JsonReader(BufferedReader(FileReader(backend))).use {
            this.homes = gson.fromJson<JsonHomeStorage>(it, this::class.java).homes
        }
        return homes.firstOrNull {it.id == id}
    }

    override fun getAll(): Set<Home> {
        return homes
    }

}