package de.exceptionflug.homesystem.request

import de.exceptionflug.homesystem.HomeSystemPlugin
import de.exceptionflug.homesystem.home.Home
import net.md_5.bungee.api.chat.ClickEvent
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.entity.Player
import java.util.concurrent.TimeUnit

class OwnerSwapRequest(duration: Long, unit: TimeUnit, val initiator: Player, val targetPlayer: Player, val home1: Home, val home2: Home) : IRequest {

    init {
        initiator.sendMessage("${HomeSystemPlugin.PREFIX} §aDu hast §6${targetPlayer.name} §aeine Anfrage gesendet.")
        targetPlayer.sendMessage("${HomeSystemPlugin.PREFIX} §6${initiator.name} §7möchte dein Haus §6${home2.name} §7gegen sein Haus §6${home1.name} §7tauschen.")
        val accept = TextComponent("§8[§a§lAnnehmen§8]")
        accept.clickEvent = ClickEvent(ClickEvent.Action.RUN_COMMAND, "/hs swapaccept ${initiator.name}")
        val reject = TextComponent("§8[§c§lAblehnen§8]")
        reject.clickEvent = ClickEvent(ClickEvent.Action.RUN_COMMAND, "/hs swapreject ${initiator.name}")
        targetPlayer.spigot().sendMessage(accept, TextComponent(" "), reject)
    }

    private val timeoutAt: Long = System.currentTimeMillis() + unit.toMillis(duration)

    override fun getTimeoutTime(): Long {
        return timeoutAt
    }

    override fun rejected(player: Player) {
        if(initiator.isOnline) {
            initiator.sendMessage("${HomeSystemPlugin.PREFIX} §cDer Spieler §6${player.name} §chat deine Anfrage abgelehnt.")
        }
    }

    override fun accepted(player: Player) {
        HomeSystemPlugin.getInstance().homeStorage.ownerSwap(home1, home2)
        if(initiator.isOnline) {
            initiator.sendMessage("${HomeSystemPlugin.PREFIX} §aDir gehört nun das Haus §6${home2.name} §avon §6${targetPlayer.name}")
        }
        if(targetPlayer.isOnline) {
            targetPlayer.sendMessage("${HomeSystemPlugin.PREFIX} §aDir gehört nun das Haus §6${home1.name} §avon §6${initiator.name}")
        }
    }

    override fun timeout() {
        if(initiator.isOnline) {
            initiator.sendMessage("${HomeSystemPlugin.PREFIX} §7Deine Anfrage an §6${targetPlayer.name} §7ist §cabgelaufen§7!")
        }
        if(targetPlayer.isOnline) {
            targetPlayer.sendMessage("${HomeSystemPlugin.PREFIX} §7Die Anfrage von §6${initiator.name} §7ist §cabgelaufen§7!")
        }
    }

}