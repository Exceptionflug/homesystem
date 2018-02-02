package de.exceptionflug.homesystem

import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin

class HomeSystemPlugin : JavaPlugin() {

    override fun onEnable() {
        Bukkit.getLogger().info("This is the HomeSystem v${description.version} by ${description.authors.toString().replace("[", "").replace("]", "")}")

    }

}