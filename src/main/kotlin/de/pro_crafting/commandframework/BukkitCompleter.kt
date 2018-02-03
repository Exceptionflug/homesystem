package de.pro_crafting.commandframework

import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import java.util.AbstractMap
import java.util.HashMap
import kotlin.collections.Map.Entry

import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter

/**
 * Command Framework - BukkitCompleter <br></br>
 * An implementation of the TabCompleter class allowing for multiple tab
 * completers per command
 *
 * @author minnymin3
 */
class BukkitCompleter : TabCompleter {

    private val completers = HashMap<String, Entry<Method, Any>>()

    fun addCompleter(label: String, m: Method, obj: Any) {
        completers[label] = AbstractMap.SimpleEntry(m, obj)
    }

    override fun onTabComplete(sender: CommandSender, command: Command, label: String, args: Array<String>): List<String>? {
        for (i in args.size downTo 0) {
            val buffer = StringBuffer()
            buffer.append(label.toLowerCase())
            for (x in 0 until i) {
                if (args[x] != "" && args[x] != " ") {
                    buffer.append("." + args[x].toLowerCase())
                }
            }
            val cmdLabel = buffer.toString()
            if (completers.containsKey(cmdLabel)) {
                val entry = completers[cmdLabel]
                try {
                    val labelParts = entry!!.key.invoke(entry.value,
                            CommandArgs(sender, command, cmdLabel, args, cmdLabel.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray().size - 1)) as List<String>
                    return if (labelParts == null || labelParts.size == 0) {
                        null
                    } else labelParts
                } catch (e: IllegalArgumentException) {
                    e.printStackTrace()
                } catch (e: IllegalAccessException) {
                    e.printStackTrace()
                } catch (e: InvocationTargetException) {
                    e.printStackTrace()
                }

            }
        }
        return null
    }

}