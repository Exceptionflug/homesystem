package de.exceptionflug.homesystem.commands

import de.exceptionflug.homesystem.HomeSystemPlugin
import de.exceptionflug.homesystem.home.Home
import de.exceptionflug.homesystem.request.TeleportRequest
import de.exceptionflug.homesystem.utils.HomePropertiesInventory
import de.exceptionflug.watertouch.WaterTouchClickableInventoryItem
import de.exceptionflug.watertouch.WaterTouchMultiPageInventory
import de.pro_crafting.commandframework.Command
import de.pro_crafting.commandframework.CommandArgs
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.function.BiConsumer
import kotlin.collections.ArrayList

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
        args.sender.sendMessage("§e/hs home §7<Name> §8- §6Teleportiert dich zu einem Haus")
        args.sender.sendMessage("§e/hs delete §7<Name> §8- §6Löscht ein Haus")
        args.sender.sendMessage("§e/hs homes §7<Name/all> §8- §6Zeigt eine Häuserliste")
        args.sender.sendMessage("§e/hs tpa §7<Spieler> <Haus> §8- §6Sendet eine Anfrage dich zu einem fremden Haus zu teleportieren")
        args.sender.sendMessage("§e/hs addmember §7<Spieler> <Haus> §8- §6Du fügst einen anderen Spieler zu deinem Haus hinzu")
        args.sender.sendMessage("§e/hs removemember §7<Spieler> <Haus> §8- §6Du entfernst einen anderen Spieler von deinem Haus")
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
            var home = HomeSystemPlugin.getInstance().homeStorage.getByName(name, p.uniqueId)
            if(home == null) {
                home = HomeSystemPlugin.getInstance().homeStorage.getByMemberWithName(p.uniqueId, name)
                if(home == null) {
                    p.sendMessage("${HomeSystemPlugin.PREFIX} §cEin Haus mit dem Namen §6${name} §ckonnte nicht gefunden werden.")
                    return
                }
            }
            p.teleport(home.location)
            p.sendMessage("${HomeSystemPlugin.PREFIX} §aDu wurdest zu einem Haus §6${name} §ateleportiert.")
        }
    }

    @Command(name = "hs.delete", description = "Löscht ein Haus", inGameOnly = true, aliases = ["delhome"], usage = "<Name>")
    fun onDelete(args: CommandArgs) {
        val p = args.player!!
        if(args.length() == 0) {
            p.sendMessage("${HomeSystemPlugin.PREFIX} §bBenutzung: /hs delete <Name>")
        } else {
            val name = args.getArgs(0)
            var home = HomeSystemPlugin.getInstance().homeStorage.getByName(name, p.uniqueId)
            if (home == null) {
                home = HomeSystemPlugin.getInstance().homeStorage.getByMemberWithName(p.uniqueId, name)
                if (home == null) {
                    p.sendMessage("${HomeSystemPlugin.PREFIX} §cEin Haus mit dem Namen §6${name} §ckonnte nicht gefunden werden.")
                    return
                }
            }
            HomeSystemPlugin.getInstance().homeStorage.delete(home)
            p.sendMessage("${HomeSystemPlugin.PREFIX} §aDas Haus §6${name} §awurde gelöscht.")
        }
    }

    @Command(name = "hs.homes", description = "Zeigt die von dir zugreifbaren Häuser an", aliases = ["homes"], inGameOnly = true, usage = "[Spieler/all]")
    fun onHomes(args: CommandArgs) {
        val p = args.player!!
        var targetUUID: UUID? = null
        if(args.length() == 0) {
            targetUUID = p.uniqueId
        } else if(!args.getArgs(0).equals("all", ignoreCase = true)) {
            val op = Bukkit.getOfflinePlayer(args.getArgs(0))
            if(op.hasPlayedBefore()) {
                targetUUID = op.uniqueId
            } else {
                p.sendMessage("${HomeSystemPlugin.PREFIX} §cDieser Spieler hat noch nie auf dem Server gespielt.")
                return
            }
        }
        p.sendMessage("${HomeSystemPlugin.PREFIX} §eEinen Moment Geduld bitte... Dieser Vorgang könnte länger dauern.")
        Bukkit.getScheduler().runTaskAsynchronously(HomeSystemPlugin.getInstance(), {
            if(targetUUID != null) {
                // We have a player to look up
                if(targetUUID != p.uniqueId &&!p.hasPermission("homesystem.homes.other")) {
                    p.sendMessage("${HomeSystemPlugin.PREFIX} §cDu hast keine Berechtigungen dazu!")
                    return@runTaskAsynchronously
                }
                val multiInv = WaterTouchMultiPageInventory("Häuser")
                for(home in HomeSystemPlugin.getInstance().homeStorage.getByOwner(targetUUID)) {
                    val memberNames = ArrayList<String>()
                    home.members
                            .map { Bukkit.getOfflinePlayer(it) }
                            .filter { it.hasPlayedBefore() }
                            .mapTo(memberNames) { it.name }
                    val item = WaterTouchClickableInventoryItem(ItemStack(Material.WOOL, 1, 5)).setDisplayName("§6${home.name}")
                            .addLoreLine("§7Position: §6${home.location.blockX} ${home.location.blockY} ${home.location.blockZ}")
                            .addLoreLine("§7Welt: §6${home.location.world.name}")
                            .addLoreLine("§7Besitzer: "+(if(targetUUID == p.uniqueId) "§aDu" else "§6"+args.getArgs(0)))
                            .addLoreLine("§7Mitbewohner: §6${memberNames.toString().replace("[", "").replace("]", "")}")
                            .addLoreLine("")
                            .addLoreLine("§7Linksklick: §6Teleportiert dich zu diesem Haus")
                            .addLoreLine("§7Rechtsklick: §6Öffnet die Einstellungen") as WaterTouchClickableInventoryItem
                    item.setOnClick(BiConsumer { t, _ ->
                        run {
                            if(t.isRightClick) {
                                HomePropertiesInventory.open(t.whoClicked as Player, home)
                            } else {
                                p.teleport(home.location)
                            }
                        }
                    })
                    multiInv.addItem(item)
                }
                for(home in HomeSystemPlugin.getInstance().homeStorage.getByMember(targetUUID)) {
                    val ownerName = Bukkit.getOfflinePlayer(home.ownerID).name
                    val memberNames = ArrayList<String>()
                    home.members
                            .map { Bukkit.getOfflinePlayer(it) }
                            .filter { it.hasPlayedBefore() }
                            .mapTo(memberNames) { it.name }
                    val item = WaterTouchClickableInventoryItem(ItemStack(Material.WOOL, 1, 1)).setDisplayName("§6${home.name}")
                            .addLoreLine("§7Position: §6${home.location.blockX} ${home.location.blockY} ${home.location.blockZ}")
                            .addLoreLine("§7Welt: §6${home.location.world.name}")
                            .addLoreLine("§7Besitzer: §6$ownerName")
                            .addLoreLine("§7Mitbewohner: §6${memberNames.toString().replace("[", "").replace("]", "")}")
                            .addLoreLine("")
                            .addLoreLine("§7Linksklick: §6Teleportiert dich zu diesem Haus")
                            .addLoreLine("§7Rechtsklick: §6Öffnet die Einstellungen") as WaterTouchClickableInventoryItem
                    item.setOnClick(BiConsumer { t, _ ->
                        run {
                            if(t.isRightClick) {
                                HomePropertiesInventory.open(t.whoClicked as Player, home)
                            } else {
                                p.teleport(home.location)
                            }
                        }
                    })
                    multiInv.addItem(item)
                }
                p.playSound(p.location, Sound.CHEST_OPEN, 1F, 1F)
                multiInv.showUp(p)
            } else {
                // We don't have a player to look up
                if(!p.hasPermission("homesystem.homes.all")) {
                    p.sendMessage("${HomeSystemPlugin.PREFIX} §cDu hast keine Berechtigungen dazu!")
                    return@runTaskAsynchronously
                }
                val multiInv = WaterTouchMultiPageInventory("Häuser")
                for(home in HomeSystemPlugin.getInstance().homeStorage.getAll()) {
                    val ownerName = Bukkit.getOfflinePlayer(home.ownerID).name
                    val memberNames = ArrayList<String>()
                    home.members
                            .map { Bukkit.getOfflinePlayer(it) }
                            .filter { it.hasPlayedBefore() }
                            .mapTo(memberNames) { it.name }
                    val item = WaterTouchClickableInventoryItem(ItemStack(Material.WOOL, 1, 1)).setDisplayName("§6${home.name}")
                            .addLoreLine("§7Position: §6${home.location.blockX} ${home.location.blockY} ${home.location.blockZ}")
                            .addLoreLine("§7Welt: §6${home.location.world.name}")
                            .addLoreLine("§7Besitzer: §6$ownerName")
                            .addLoreLine("§7Mitbewohner: §6${memberNames.toString().replace("[", "").replace("]", "")}")
                            .addLoreLine("")
                            .addLoreLine("§7Linksklick: §6Teleportiert dich zu diesem Haus")
                            .addLoreLine("§7Rechtsklick: §6Öffnet die Einstellungen") as WaterTouchClickableInventoryItem
                    item.setOnClick(BiConsumer { t, _ ->
                        run {
                            if(t.isRightClick) {
                                HomePropertiesInventory.open(t.whoClicked as Player, home)
                            } else {
                                p.teleport(home.location)
                            }
                        }
                    })
                    multiInv.addItem(item)
                }
                p.playSound(p.location, Sound.CHEST_OPEN, 1F, 1F)
                multiInv.showUp(p)
            }
        })
    }

    @Command(name = "hs.tpa", description = "Sendet eine Anfrage dich zu einem fremden Haus zu teleportieren", aliases = ["tpa"], usage = "<Spieler> <Haus>", inGameOnly = true)
    fun onTPA(args: CommandArgs) {
        val p = args.player!!
        if(args.length() < 2) {
            p.sendMessage("${HomeSystemPlugin.PREFIX} §bBenutzung: /hs tpa <Spieler> <Haus>")
        } else {
            val targetPlayer = Bukkit.getPlayerExact(args.getArgs(0))
            if(targetPlayer == null) {
                p.sendMessage("${HomeSystemPlugin.PREFIX} §cDer angegebene Spieler ist nicht online.")
                return
            }
            if(targetPlayer.uniqueId == p.uniqueId) {
                p.sendMessage("${HomeSystemPlugin.PREFIX} §cDu kannst dir keine Anfragen schicken.")
                return
            }
            val home = HomeSystemPlugin.getInstance().homeStorage.getByName(args.getArgs(1), targetPlayer.uniqueId)
            if(home == null) {
                p.sendMessage("${HomeSystemPlugin.PREFIX} §cDer Spieler §6${targetPlayer.name} §chat kein Haus mit dem Namen §6${args.getArgs(1)}§c.")
                return
            }
            if(getTeleportRequestFromTo(p, targetPlayer) != null) {
                p.sendMessage("${HomeSystemPlugin.PREFIX} §cDu kannst einem Spieler maximal einen Anfrage senden!")
                return
            }
            HomeSystemPlugin.getInstance().requestManager.requests.add(TeleportRequest(60, TimeUnit.SECONDS, p, targetPlayer, home))
        }
    }

    @Command(name = "hs.tpaccept", description = "Nimmt eine TPAnfrage an", inGameOnly = true, aliases = ["tpaccept"], usage = "<Spieler>")
    fun onTPAccept(args: CommandArgs) {
        val p = args.player!!
        if(args.length() == 0) {
            p.sendMessage("${HomeSystemPlugin.PREFIX} §bBenutzung: /hs tpaccept <Spieler>")
        } else {
            val targetPlayer = Bukkit.getPlayerExact(args.getArgs(0))
            if(targetPlayer == null) {
                p.sendMessage("${HomeSystemPlugin.PREFIX} §cDer angegebene Spieler ist nicht online")
                return
            }
            val request = getTeleportRequestFromTo(targetPlayer, p)
            if(request == null) {
                p.sendMessage("${HomeSystemPlugin.PREFIX} §cDieser Spieler hat dir keine Anfrage gesendet.")
                return
            }
            HomeSystemPlugin.getInstance().requestManager.accept(p, request)
        }
    }

    @Command(name = "hs.tpreject", description = "Lehnt eine TPAnfrage ab", inGameOnly = true, aliases = ["tpreject"], usage = "<Spieler>")
    fun onTPReject(args: CommandArgs) {
        val p = args.player!!
        if(args.length() == 0) {
            p.sendMessage("${HomeSystemPlugin.PREFIX} §bBenutzung: /hs tpreject <Spieler>")
        } else {
            val targetPlayer = Bukkit.getPlayerExact(args.getArgs(0))
            if(targetPlayer == null) {
                p.sendMessage("${HomeSystemPlugin.PREFIX} §cDer angegebene Spieler ist nicht online")
                return
            }
            val request = getTeleportRequestFromTo(targetPlayer, p)
            if(request == null) {
                p.sendMessage("${HomeSystemPlugin.PREFIX} §cDieser Spieler hat dir keine Anfrage gesendet.")
                return
            }
            HomeSystemPlugin.getInstance().requestManager.reject(p, request)
        }
    }

    @Command(name = "hs.addmember", description = "Fügt einen Spieler zu einem Haus hinzu", inGameOnly = true, usage = "<Spieler> <Haus>")
    fun onAddMember(args: CommandArgs) {
        val p = args.player!!
        if(args.length() < 2) {
            p.sendMessage("${HomeSystemPlugin.PREFIX} §bBenutzung: /hs addmember <Spieler> <Haus>")
        } else {
            val offlinePlayer = Bukkit.getOfflinePlayer(args.getArgs(0))
            if(!offlinePlayer.hasPlayedBefore()) {
                p.sendMessage("${HomeSystemPlugin.PREFIX} §cDieser Spieler hat noch nie zuvor auf diesem Server gespielt.")
                return
            }
            val home = HomeSystemPlugin.getInstance().homeStorage.getByName(args.getArgs(1), p.uniqueId)
            if(home == null) {
                p.sendMessage("${HomeSystemPlugin.PREFIX} §cDas Haus §6${args.getArgs(1)} §cexistiert nicht.")
                return
            }
            if(p.uniqueId == offlinePlayer.uniqueId) {
                p.sendMessage("${HomeSystemPlugin.PREFIX} §cDu kannst dich nicht zu deinen eigenen Häusern hinzufügen.")
                return
            }
            if(home.members.contains(offlinePlayer.uniqueId)) {
                p.sendMessage("${HomeSystemPlugin.PREFIX} §6${args.getArgs(0)} §cist bereits Mitglied von §6${args.getArgs(1)}§c.")
                return
            }
            home.members.add(offlinePlayer.uniqueId)
            HomeSystemPlugin.getInstance().homeStorage.save(home)
            p.sendMessage("${HomeSystemPlugin.PREFIX} §6${offlinePlayer.name} §awurde zu §6${home.name} §ahinzugefügt.")
        }
    }

    @Command(name = "hs.removemember", description = "Entfernt einen Mitbewohner", inGameOnly = true, usage = "<Spieler> <Haus>")
    fun onRemoveMember(args: CommandArgs) {
        val p = args.player!!
        if(args.length() < 2) {
            p.sendMessage("${HomeSystemPlugin.PREFIX} §bBenutzung: /hs removemember <Spieler> <Haus>")
        } else {
            val offlinePlayer = Bukkit.getOfflinePlayer(args.getArgs(0))
            if(!offlinePlayer.hasPlayedBefore()) {
                p.sendMessage("${HomeSystemPlugin.PREFIX} §cDieser Spieler hat noch nie zuvor auf diesem Server gespielt.")
                return
            }
            val home = HomeSystemPlugin.getInstance().homeStorage.getByName(args.getArgs(1), p.uniqueId)
            if(home == null) {
                p.sendMessage("${HomeSystemPlugin.PREFIX} §cDas Haus §6${args.getArgs(1)} §cexistiert nicht.")
                return
            }
            if(!home.members.contains(offlinePlayer.uniqueId)) {
                p.sendMessage("${HomeSystemPlugin.PREFIX} §6${args.getArgs(0)} §cist kein Mitglied von §6${args.getArgs(1)}§c.")
                return
            }
            home.members.remove(offlinePlayer.uniqueId)
            HomeSystemPlugin.getInstance().homeStorage.save(home)
            p.sendMessage("${HomeSystemPlugin.PREFIX} §6${offlinePlayer.name} §awurde von §6${home.name} §aentfernt.")
        }
    }

    private fun getTeleportRequestFromTo(player: Player, targetPlayer: Player): TeleportRequest? {
        return HomeSystemPlugin.getInstance().requestManager.requests
                .filterIsInstance<TeleportRequest>()
                .firstOrNull { it.initiator.uniqueId == player.uniqueId && it.targetPlayer.uniqueId == targetPlayer.uniqueId }
    }

}