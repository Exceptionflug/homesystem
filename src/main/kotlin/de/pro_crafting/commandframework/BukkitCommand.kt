package de.pro_crafting.commandframework

import org.apache.commons.lang.Validate
import org.bukkit.command.CommandException
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.plugin.Plugin

/**
 * Command Framework - BukkitCommand <br></br>
 * An implementation of Bukkit's Command class allowing for registering of
 * commands without plugin.yml
 *
 * @author minnymin3
 */
class BukkitCommand
/**
 * A slimmed down PluginCommand
 *
 * @param name
 * @param owner
 */
(label: String, private val executor: CommandExecutor, private val owningPlugin: Plugin) : org.bukkit.command.Command(label) {
    var completer: BukkitCompleter? = null

    init {
        this.usageMessage = ""
    }

    override fun execute(sender: CommandSender, commandLabel: String, args: Array<String>): Boolean {
        var success = false

        if (!owningPlugin.isEnabled) {
            return false
        }

        if (!testPermission(sender)) {
            return true
        }

        try {
            success = executor.onCommand(sender, this, commandLabel, args)
        } catch (ex: Throwable) {
            throw CommandException("Unhandled exception executing command '" + commandLabel + "' in plugin "
                    + owningPlugin.description.fullName, ex)
        }

        if (!success && usageMessage.length > 0) {
            for (line in usageMessage.replace("<command>", commandLabel).split("\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()) {
                sender.sendMessage(line)
            }
        }

        return success
    }

    @Throws(CommandException::class, IllegalArgumentException::class)
    override fun tabComplete(sender: CommandSender, alias: String, args: Array<String>): List<String> {
        Validate.notNull(sender, "Sender cannot be null")
        Validate.notNull(args, "Arguments cannot be null")
        Validate.notNull(alias, "Alias cannot be null")

        var completions: List<String>? = null
        try {
            if (completer != null) {
                completions = completer!!.onTabComplete(sender, this, alias, args)
            }
            if (completions == null && executor is TabCompleter) {
                completions = (executor as TabCompleter).onTabComplete(sender, this, alias, args)
            }
        } catch (ex: Throwable) {
            val message = StringBuilder()
            message.append("Unhandled exception during tab completion for command '/").append(alias).append(' ')
            for (arg in args) {
                message.append(arg).append(' ')
            }
            message.deleteCharAt(message.length - 1).append("' in plugin ")
                    .append(owningPlugin.description.fullName)
            throw CommandException(message.toString(), ex)
        }

        return if (completions == null) {
            super.tabComplete(sender, alias, args)
        } else completions
    }

}
