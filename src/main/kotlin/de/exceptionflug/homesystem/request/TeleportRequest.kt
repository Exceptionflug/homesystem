package de.exceptionflug.homesystem.request

import de.exceptionflug.homesystem.HomeSystemPlugin
import de.exceptionflug.homesystem.home.Home
import net.md_5.bungee.api.chat.ClickEvent
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.entity.Player
import java.util.concurrent.TimeUnit

class TeleportRequest(duration: Long, unit: TimeUnit, val initiator: Player, val targetPlayer: Player, val toHome: Home) : IRequest {

    init {
        initiator.sendMessage("${HomeSystemPlugin.PREFIX} §aDu hast §6${targetPlayer.name} §aeine Anfrage gesendet.")
        targetPlayer.sendMessage("${HomeSystemPlugin.PREFIX} §6${initiator.name} §7möchte sich zu deinem Haus §6${toHome.name} §7teleportieren.")
        val accept = TextComponent("§8[§a§lAnnehmen§8]")
        accept.clickEvent = ClickEvent(ClickEvent.Action.RUN_COMMAND, "/hs tpaccept ${initiator.name}")
        val reject = TextComponent("§8[§c§lAblehnen§8]")
        reject.clickEvent = ClickEvent(ClickEvent.Action.RUN_COMMAND, "/hs tpreject ${initiator.name}")
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
        if(initiator.isOnline) {
            initiator.sendMessage("${HomeSystemPlugin.PREFIX} §aDer Spieler §6${player.name} §ahat deine Anfrage angenommen.")
            player.teleport(toHome.location)
        } else {
            player.sendMessage("${HomeSystemPlugin.PREFIX} §6${initiator.name} §cist leider offline :(")
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