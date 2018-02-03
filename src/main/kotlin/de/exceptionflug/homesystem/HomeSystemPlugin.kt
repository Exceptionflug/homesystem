package de.exceptionflug.homesystem

import de.exceptionflug.homesystem.commands.CommandHomesystem
import de.exceptionflug.homesystem.mysql.ConnectionHolder
import de.exceptionflug.homesystem.mysql.HomeSQLObject
import de.exceptionflug.homesystem.mysql.OwnerSwapSQLObject
import de.exceptionflug.homesystem.request.RequestManager
import de.exceptionflug.homesystem.storage.IHomeStorage
import de.exceptionflug.homesystem.storage.JsonHomeStorage
import de.exceptionflug.homesystem.storage.MySQLHomeStorage
import de.exceptionflug.litboards.LitBoard
import de.exceptionflug.watertouch.WaterTouchInventoryController
import de.pro_crafting.commandframework.CommandArgs
import de.pro_crafting.commandframework.CommandFramework
import de.pro_crafting.commandframework.Completer
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.plugin.java.JavaPlugin
import java.io.File
import java.net.URI
import java.util.*
import java.util.logging.Level
import java.util.logging.Logger

class HomeSystemPlugin : JavaPlugin(), Listener {

    lateinit var homeStorage: IHomeStorage
        private set

    lateinit var requestManager: RequestManager
        private set

    var notificationRadii: Int = 50
        private set

    private val mapPlayerToScoreboard = HashMap<UUID, PlayerScoreboard>()

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
        WaterTouchInventoryController()
        requestManager = RequestManager()
        Bukkit.getPluginManager().registerEvents(this, this)
    }

    private fun loadYamlConfig() {
        config.options().copyDefaults(true)
        config.options().header("Configuration file of the HomeSystem")
        config.addDefault("backend.type", "json")
        config.addDefault("backend.uri", File("./plugins/HomeSystem/homes.json").absoluteFile.toURI().toString())
        config.addDefault("settings.notification-radii", 50)
        saveConfig()
        backendType = config.getString("backend.type", "json")
        val uri = URI(config.getString("backend.uri", File("./plugins/HomeSystem/homes.json").absoluteFile.toURI().toString()))
        if(backendType == "json") {
            homeStorage = JsonHomeStorage(File(uri))
        } else if(backendType == "mysql") {
            val connectionHolder = ConnectionHolder(this)
            connectionHolder.connect(uri)
            HomeSQLObject(connectionHolder).createTable()
            OwnerSwapSQLObject(connectionHolder).createTable()
            homeStorage = MySQLHomeStorage(connectionHolder)
        }
        notificationRadii = config.getInt("settings.notification-radii", 50)
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

    // ===============================================================================================
    // Small event-handling section. Please don't kill me because I placed this in the main class :(
    // ===============================================================================================

    @EventHandler
    fun onJoin(e: PlayerJoinEvent) {
        if(!::homeStorage.isInitialized) {
            Bukkit.getLogger().warning("[HomeSystem] No backend was initialized! Please check your config.")
            return
        }
        val board = PlayerScoreboard(e.player)
        mapPlayerToScoreboard[e.player.uniqueId] = board
    }

    @EventHandler
    fun onQuit(e: PlayerQuitEvent) {
        mapPlayerToScoreboard[e.player.uniqueId]?.stop()
        mapPlayerToScoreboard.remove(e.player.uniqueId)
    }

    companion object {
        const val PREFIX: String = "§8[§6Home§eSystem§8]§7"
        fun getInstance(): HomeSystemPlugin {
            return getPlugin(HomeSystemPlugin::class.java)
        }
    }

}