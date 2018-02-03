package de.exceptionflug.homesystem.commands

import de.exceptionflug.homesystem.HomeSystemPlugin
import de.exceptionflug.homesystem.home.Home
import de.pro_crafting.commandframework.Command
import de.pro_crafting.commandframework.CommandArgs
import java.util.*

class CommandHomesystem {

    @Command(name = "hs", description = "Hauptbefehl")
    fun onHS(args: CommandArgs) {
        args.sender.sendMessage("${HomeSystemPlugin.PREFIX} HomeSystem by ${HomeSystemPlugin.getInstance().description.authors.toString().replace("[", "").replace("]", "")} (C) 2018")
        args.sender.sendMessage("${HomeSystemPlugin.PREFIX} Version: §6${HomeSystemPlugin.getInstance().description.version}")
        args.sender.sendMessage("${HomeSystemPlugin.PREFIX} Um eine Liste mit Befehlen zu erhalten, gib §6/hs help §7ein.")
    }

    @Command(name = "hs.help", description = "Hilfe")
    fun onHelp(args: CommandArgs) {
        args.sender.sendMessage("${HomeSystemPlugin.PREFIX} Befehle:")
        args.sender.sendMessage("§e/hs §8- §6Zeigt dir Informationen über das Plugin")
        args.sender.sendMessage("§e/hs help §8- §6Zeigt dir diese Seite")
        args.sender.sendMessage("§e/hs sethome §7<Name> §8- §6Setzt ein Haus mit einem Namen")
        args.sender.sendMessage("§8/hs home §7<Name> §8- §6Teleportiert dich zu deinem Haus")
    }

    @Command(name = "hs.sethome", description = "Setzt ein Haus", inGameOnly = true, aliases = ["sethome"], usage = "<Name>")
    fun onSethome(args: CommandArgs) {
        val p = args.player!!
        if(args.length() == 0) {
            p.sendMessage("${HomeSystemPlugin.PREFIX} §bBenutzung: /hs sethome <Name>")
        } else {
            val name = args.getArgs(0)
            val home = HomeSystemPlugin.getInstance().homeStorage.getByName(name, p.uniqueId) ?: Home(UUID.randomUUID(), p.uniqueId, p.location)
            home.name = name
            home.location = p.location
            HomeSystemPlugin.getInstance().homeStorage.save(home)
            p.sendMessage("${HomeSystemPlugin.PREFIX} §aDein Haus §6${name} §awurde gesetzt!")
        }
    }

    @Command(name = "hs.home", description = "Teleportiert dich zu deinem Haus", inGameOnly = true, aliases = ["home"], usage = "<Name>")
    fun onHome(args: CommandArgs) {
        val p = args.player!!
        if(args.length() == 0) {
            p.sendMessage("${HomeSystemPlugin.PREFIX} §bBenutzung: /hs home <Name>")
        } else {
            val name = args.getArgs(0)
            val home = HomeSystemPlugin.getInstance().homeStorage.getByName(name, p.uniqueId)
            if(home == null) {
                p.sendMessage("${HomeSystemPlugin.PREFIX} §cDein Haus mit dem Namen §6${name} §ckonnte nicht gefunden werden.")
                return
            }
            p.teleport(home.location)
            p.sendMessage("${HomeSystemPlugin.PREFIX} §aDu wurdest zu deinem Haus §6${name} §ateleportiert.")
        }
    }

}