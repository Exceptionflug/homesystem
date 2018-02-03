package de.exceptionflug.homesystem.storage

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.annotations.Expose
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import de.exceptionflug.homesystem.home.Home
import de.exceptionflug.homesystem.home.IOwnerSwap
import de.exceptionflug.homesystem.utils.LocationJsonSerializer
import org.bukkit.Location
import java.io.*
import java.util.*
import kotlin.collections.HashSet

class JsonHomeStorage(@Transient val backend: File) : IHomeStorage {

    companion object {
        val GSON: Gson = GsonBuilder().registerTypeAdapter(Location::class.java, LocationJsonSerializer()).setPrettyPrinting().create()
    }

    private var homes: MutableSet<Home> = HashSet()
    private var swaps: MutableSet<JsonOwnerSwap> = HashSet()

    init {
        if(!backend.exists()) {
            backend.createNewFile()
        } else if(backend.isDirectory) throw IllegalStateException("The given file is a directory!")
        JsonReader(BufferedReader(FileReader(backend))).use {
            try {
                val jsonHomeStorage = GSON.fromJson<JsonHomeStorage>(it, this::class.java)
                this.homes = jsonHomeStorage.homes
                this.swaps = jsonHomeStorage.swaps
            } catch (e: NullPointerException) {
                // If the file is completely empty, an exception will be thrown while attempting to parse it
            }
        }
    }

    override fun save(home: Home) {
        if(!homes.contains(home)) {
            homes.add(home)
        }
        val json = GSON.toJson(this, this::class.java).split("\n")
        val printWriter = PrintWriter(FileWriter(backend))
        for(line in json) {
            printWriter.println(line)
        }
        printWriter.flush()
        printWriter.close()
    }

    override fun load(id: UUID): Home? {
        for(home in homes) {
            if(home.id == id) return home
        }
        // If the desired home is not in memory, try to reload the backend
        JsonReader(BufferedReader(FileReader(backend))).use {
            try {
                val jsonHomeStorage = GSON.fromJson<JsonHomeStorage>(it, this::class.java)
                this.homes = jsonHomeStorage.homes
                this.swaps = jsonHomeStorage.swaps
            } catch (e: NullPointerException) {
                // If the file is completely empty, an exception will be thrown while attempting to parse it
            }
        }
        return homes.firstOrNull {it.id == id}
    }

    override fun getAll(): Set<Home> {
        return homes
    }

    override fun ownerSwap(h1: Home, h2: Home) {
        val swap = JsonOwnerSwap(h1.id, h2.id, System.currentTimeMillis())
        swaps.add(swap)
        val uuidH1 = h1.ownerID
        val uuidH2 = h2.ownerID
        h1.ownerID = uuidH2
        h2.ownerID = uuidH1
        GSON.toJson(this, this::class.java, JsonWriter(FileWriter(backend)))
    }

    override fun getAllSwaps(): Set<IOwnerSwap> {
        return swaps
    }

}