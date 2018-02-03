package de.exceptionflug.homesystem

import de.exceptionflug.homesystem.commands.CommandHomesystem
import de.exceptionflug.homesystem.mysql.ConnectionHolder
import de.exceptionflug.homesystem.mysql.HomeSQLObject
import de.exceptionflug.homesystem.mysql.OwnerSwapSQLObject
import de.exceptionflug.homesystem.storage.IHomeStorage
import de.exceptionflug.homesystem.storage.JsonHomeStorage
import de.exceptionflug.homesystem.storage.MySQLHomeStorage
import de.pro_crafting.commandframework.CommandArgs
import de.pro_crafting.commandframework.CommandFramework
import de.pro_crafting.commandframework.Completer
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin
import java.io.File
import java.net.URI
import java.util.logging.Level

class HomeSystemPlugin : JavaPlugin() {

    lateinit var homeStorage: IHomeStorage
        private set

    private lateinit var backendType: String
    private lateinit var commandFramework: CommandFramework

    override fun onEnable() {
        Bukkit.getLogger().info("This is the HomeSystem v${description.version} by ${description.authors.toString().replace("[", "").replace("]", "")}")
        try {
            loadYamlConfig()
        } catch(t: Throwable) {
            Bukkit.getLogger().log(Level.SEVERE, "An error occurred while loading config", t)
        }
        commandFramework = CommandFramework(this)
        val methods = this.javaClass.methods
        methods
                .filter { it.name.equals("completeCommands", ignoreCase = true) }
                .forEach { this.commandFramework.registerCompleter("hs", it, this) }
        commandFramework.registerCommands(CommandHomesystem())
        commandFramework.registerHelp()
        commandFramework.inGameOnlyMessage = "§cNur ein Spieler darf diesen Befehl eingeben!"
    }

    private fun loadYamlConfig() {
        config.options().copyDefaults(true)
        config.options().header("Configuration file of the HomeSystem")
        config.addDefault("backend.type", "json")
        config.addDefault("backend.uri", File("./plugins/HomeSystem/homes.json").absoluteFile.toURI().toString())
        saveConfig()
        backendType = config.getString("backend.type")
        val uri = URI(config.getString("backend.uri"))
        if(backendType == "json") {
            homeStorage = JsonHomeStorage(File(uri))
        } else if(backendType == "mysql") {
            val connectionHolder = ConnectionHolder(this)
            connectionHolder.connect(uri)
            HomeSQLObject(connectionHolder).createTable()
            OwnerSwapSQLObject(connectionHolder).createTable()
            homeStorage = MySQLHomeStorage(connectionHolder)
        }
    }

    @Completer(name = "hs")
    fun completeCommands(args: CommandArgs): List<String> {
        val ret = ArrayList<String>()
        var label = args.command.label
        for (arg in args.args) {
            label += " " + arg
        }
        for (currentLabel in this.commandFramework.commandLabels) {
            if (currentLabel.contains("meSystem")) {
                continue
            }
            var current = currentLabel.replace('.', ' ')
            if (current.contains(label)) {
                current = current.substring(label.lastIndexOf(' ')).trim({ it <= ' ' })
                current = current.substring(0, if (current.indexOf(' ') != -1) current.indexOf(' ') else current.length)
                        .trim({ it <= ' ' })
                if (!ret.contains(current)) {
                    ret.add(current)
                }
            }
        }
        return ret
    }

    companion object {
        val PREFIX: String = "§8[§6Home§eSystem§8]§7"
        fun getInstance(): HomeSystemPlugin {
            return getPlugin(HomeSystemPlugin::class.java)
        }
    }

}