package de.pro_crafting.commandframework

import java.lang.reflect.Field
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import java.util.AbstractMap
import java.util.HashMap
import kotlin.collections.Map.Entry
import java.util.TreeSet

import org.bukkit.Bukkit
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandMap
import org.bukkit.command.CommandSender
import org.bukkit.command.PluginCommand
import org.bukkit.entity.Player
import org.bukkit.help.GenericCommandHelpTopic
import org.bukkit.help.HelpTopic
import org.bukkit.help.HelpTopicComparator
import org.bukkit.help.IndexHelpTopic
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.SimplePluginManager

/**
 * Command Framework - CommandFramework <br></br>
 * The main command framework class used for controlling the framework.
 *
 * @author minnymin3
 */
class CommandFramework
/**
 * Initializes the command framework and sets up the command maps
 */
(private val plugin: Plugin) : CommandExecutor {

    private val commandMap = HashMap<String, Entry<Method, Any>>()
    private var map: CommandMap? = null
    var noPermMessage: String? = null

    var inGameOnlyMessage: String? = null

    val commandLabels: Array<String>
        get() = this.commandMap.keys.toTypedArray()

    init {
        if (plugin.server.pluginManager is SimplePluginManager) {
            val manager = plugin.server.pluginManager as SimplePluginManager
            try {
                val field = SimplePluginManager::class.java.getDeclaredField("commandMap")
                field.isAccessible = true
                map = field.get(manager) as CommandMap
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }

        this.inGameOnlyMessage = "This command is only performable in game"
    }

    override fun onCommand(sender: CommandSender, cmd: org.bukkit.command.Command, label: String, args: Array<String>): Boolean {
        return handleCommand(sender, cmd, label, args)
    }

    /**
     * Handles commands. Used in the onCommand method in your JavaPlugin class
     *
     * @param sender The [org.bukkit.command.CommandSender] parsed from
     * onCommand
     * @param cmd The [org.bukkit.command.Command] parsed from onCommand
     * @param label The label parsed from onCommand
     * @param args The arguments parsed from onCommand
     * @return Always returns true for simplicity's sake in onCommand
     */
    fun handleCommand(sender: CommandSender, cmd: org.bukkit.command.Command, label: String, args: Array<String>): Boolean {
        for (i in args.size downTo 0) {
            val buffer = StringBuffer()
            buffer.append(label.toLowerCase())
            for (x in 0 until i) {
                buffer.append(".").append(args[x].toLowerCase())
            }
            val cmdLabel = buffer.toString()
            if (commandMap.containsKey(cmdLabel)) {
                val method = commandMap[cmdLabel]!!.key
                val methodObject = commandMap[cmdLabel]!!.value
                val command = method.getAnnotation(Command::class.java)
                if (command.permission != null && !command.permission.isEmpty() && !sender.hasPermission(command.permission)) {
                    if (command.noPerm != "You do not have permission to perform that action" || this.noPermMessage == null) {
                        sender.sendMessage(command.noPerm)
                        return true
                    }

                    sender.sendMessage(this.noPermMessage)
                    return true


                }
                if (command.inGameOnly && sender !is Player) {
                    sender.sendMessage(this.inGameOnlyMessage)
                    return true
                }
                try {
                    method.invoke(methodObject, CommandArgs(sender, cmd, label, args,
                            cmdLabel.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray().size - 1))
                } catch (e: IllegalArgumentException) {
                    e.printStackTrace()
                } catch (e: IllegalAccessException) {
                    e.printStackTrace()
                } catch (e: InvocationTargetException) {
                    e.printStackTrace()
                }

                return true
            }
        }
        defaultCommand(CommandArgs(sender, cmd, label, args, 0))
        return true
    }

    /**
     * Registers all command and completer methods inside of the object. Similar
     * to Bukkit's registerEvents method.
     *
     * @param obj The object to register the commands of
     */
    fun registerCommands(obj: Any) {
        for (m in obj.javaClass.methods) {
            if (m.getAnnotation(Command::class.java) != null) {
                val command = m.getAnnotation(Command::class.java)
                if (m.parameterTypes.size != 1 || m.parameterTypes[0] != CommandArgs::class.java) {
                    plugin.logger.warning("Unable to register command " + m.name +
                            ". Unexpected method arguments")
                    continue
                }
                registerCommand(command, command.name, m, obj)
                for (alias in command.aliases) {
                    registerCommand(command, alias, m, obj)
                }
            } else if (m.getAnnotation(Completer::class.java) != null) {
                val comp = m.getAnnotation(Completer::class.java)
                if (m.parameterTypes.size > 1 || m.parameterTypes.size == 0 || m.parameterTypes[0] != CommandArgs::class.java) {
                    plugin.logger.warning("Unable to register tab completer " + m.name +
                            ". Unexpected method arguments")
                    continue
                }
                if (m.returnType != List::class.java) {
                    plugin.logger.warning("Unable to register command " + m.name +
                            ". Unexpected return type")
                    continue
                }
                registerCompleter(comp.name, m, obj)
                for (alias in comp.aliases) {
                    registerCompleter(alias, m, obj)
                }
            }
        }
    }

    /**
     * Registers all the commands under the plugin's help
     */
    fun registerHelp() {
        val help = TreeSet(HelpTopicComparator.helpTopicComparatorInstance())
        for (s in commandMap.keys) {
            if (!s.contains(".")) {
                val cmd = map!!.getCommand(s)
                val topic = GenericCommandHelpTopic(cmd)
                help.add(topic)
            }
        }
        val topic = IndexHelpTopic(plugin.name, "All commands for " + plugin.name, null, help,
                "Below is a list of all " + plugin.name + " commands:")
        Bukkit.getServer().helpMap.addTopic(topic)
    }

    fun registerCommand(command: Command, label: String, m: Method, obj: Any) {
        commandMap[label.toLowerCase()] = AbstractMap.SimpleEntry(m, obj)
        commandMap[this.plugin.name + ':'.toString() + label.toLowerCase()] = AbstractMap.SimpleEntry(m, obj)
        val cmdLabel = label.replace(".", ",").split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0].toLowerCase()
        if (map!!.getCommand(cmdLabel) == null) {
            val cmd = BukkitCommand(cmdLabel, this, plugin)
            map!!.register(plugin.name, cmd)
        }
        if (!command.description.equals("", ignoreCase = true) && cmdLabel == label) {
            map!!.getCommand(cmdLabel).description = command.description
        }
        if (!command.usage.equals("", ignoreCase = true) && cmdLabel == label) {
            map!!.getCommand(cmdLabel).usage = command.usage
        }
    }

    fun registerCompleter(label: String, m: Method, obj: Any) {
        val cmdLabel = label.replace(".", ",").split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0].toLowerCase()
        if (map!!.getCommand(cmdLabel) == null) {
            val command = BukkitCommand(cmdLabel, this, plugin)
            map!!.register(plugin.name, command)
        }
        if (map!!.getCommand(cmdLabel) is BukkitCommand) {
            val command = map!!.getCommand(cmdLabel) as BukkitCommand
            if (command.completer == null) {
                command.completer = BukkitCompleter()
            }
            command.completer!!.addCompleter(label, m, obj)
        } else if (map!!.getCommand(cmdLabel) is PluginCommand) {
            try {
                val command = map!!.getCommand(cmdLabel)
                val field = command.javaClass.getDeclaredField("completer")
                field.isAccessible = true
                if (field.get(command) == null) {
                    val completer = BukkitCompleter()
                    completer.addCompleter(label, m, obj)
                    field.set(command, completer)
                } else if (field.get(command) is BukkitCompleter) {
                    val completer = field.get(command) as BukkitCompleter
                    completer.addCompleter(label, m, obj)
                } else {
                    plugin.logger.warning("Unable to register tab completer " + m.name
                            + ". A tab completer is already registered for that command!")
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
            }

        }
    }

    private fun defaultCommand(args: CommandArgs) {
        args.sender.sendMessage(args.label + " is not handled! Oh noes!")
    }

}
