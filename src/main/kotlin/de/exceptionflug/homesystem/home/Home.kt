package de.exceptionflug.homesystem.home

import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.Player
import java.util.*
import kotlin.collections.HashSet

class Home(val id: UUID, var ownerID: UUID, val location: Location) {

    val members: MutableSet<UUID> = HashSet()

    fun getOwner(): Player? {
        return Bukkit.getPlayer(ownerID)
    }

    fun getMembersOnline(): Set<Player> {
        val members = HashSet<Player>()
        for(uuid in this.members) {
            val player = Bukkit.getPlayer(uuid)
            if(player != null) members.add(player)
        }
        return members
    }

}