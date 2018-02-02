package de.exceptionflug.homesystem

import de.exceptionflug.homesystem.mysql.ConnectionHolder
import de.exceptionflug.homesystem.storage.IHomeStorage
import de.exceptionflug.homesystem.storage.JsonHomeStorage
import de.exceptionflug.homesystem.storage.MySQLHomeStorage
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin
import java.io.File
import java.net.URI
import java.util.logging.Level

class HomeSystemPlugin : JavaPlugin() {

    lateinit var homeStorage: IHomeStorage
        private set

    private lateinit var backendType: String

    override fun onEnable() {
        Bukkit.getLogger().info("This is the HomeSystem v${description.version} by ${description.authors.toString().replace("[", "").replace("]", "")}")
        try {
            loadYamlConfig()
        } catch(t: Throwable) {
            Bukkit.getLogger().log(Level.SEVERE, "An error occurred while loading config", t)
        }
    }

    private fun loadYamlConfig() {
        config.options().copyDefaults(true)
        config.options().header("Configuration file of the HomeSystem")
        config.addDefault("backend.type", "json")
        config.addDefault("backend.uri", File("./plugins/HomeSystem/homes.json").toURI().toString())
        saveConfig()
        backendType = config.getString("backend.type")
        val uri = URI(config.getString("backend.uri"))
        if(backendType.equals("json")) {
            homeStorage = JsonHomeStorage(File(uri))
        } else if(backendType.equals("mysql")) {
            val connectionHolder = ConnectionHolder(this)
            connectionHolder.connect(uri)
            homeStorage = MySQLHomeStorage(connectionHolder)
        }
    }

    companion object {
        fun getInstance(): HomeSystemPlugin {
            return getPlugin(HomeSystemPlugin::class.java)
        }
    }

}