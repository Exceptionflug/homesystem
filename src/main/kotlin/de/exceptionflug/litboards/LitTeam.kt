package de.exceptionflug.litboards

import com.comphenix.packetwrapper.WrapperPlayServerScoreboardTeam
import de.exceptionflug.homesystem.utils.ReflectionUtil
import org.bukkit.Bukkit
import java.util.*

class LitTeam internal constructor(val board: LitBoard, val name: String) {
    var displayName: String? = null
        set(value) {
            field = value
            update()
        }
    var suffix: String? = null
        set(value) {
            field = value
            update()
        }
    var prefix: String? = null
        set(value) {
            field = value
            update()
        }
    var isFriendlyFire = true
        set(friendlyFire) {
            field = friendlyFire
            update()
        }

    var nameTagVisibility = NameTagVisibility.ALWAYS
        set(nameTagVisibility) {
            field = nameTagVisibility
            update()
        }

    var collisionRule = CollisionRule.NEVER
        set(collisionRule) {
            field = collisionRule
            update()
        }

    var color = -1
        set(color) {
            field = color
            update()
        }

    val members = ArrayList<String>()

    init {
        this.displayName = name
        this.prefix = ""
        this.suffix = ""
    }

    fun update() {
        val pck = WrapperPlayServerScoreboardTeam()
        pck.name = name
        pck.mode = 2
        pck.prefix = prefix
        pck.displayName = displayName
        pck.suffix = suffix
        pck.packOptionData = if (isFriendlyFire) 0x01 else 0x02
        pck.nameTagVisibility = this.nameTagVisibility.serialized
        if (!ReflectionUtil.version.startsWith("v1_8")) {
            pck.collisionRule = this.collisionRule.serialized
        }
        pck.color = this.color
        for (id in LitBoard.getBoardHolders(board)) {
            if (id == null) continue
            val z = Bukkit.getPlayer(id) ?: continue
            pck.sendPacket(z)
        }
    }

    fun addMember(name: String) {
        members.add(name)
        val pck = WrapperPlayServerScoreboardTeam()
        pck.name = this.name
        pck.mode = 3
        val list = ArrayList<String>()
        list.add(name)
        pck.players = list
        for (id in LitBoard.getBoardHolders(board)) {
            if (id == null) continue
            val z = Bukkit.getPlayer(id) ?: continue
            pck.sendPacket(z)
        }
    }

    fun removeMember(name: String) {
        members.remove(name)
        val pck = WrapperPlayServerScoreboardTeam()
        pck.name = this.name
        pck.mode = 4
        val list = ArrayList<String>()
        list.add(name)
        pck.players = list
        for (id in LitBoard.getBoardHolders(board)) {
            if (id == null) continue
            val z = Bukkit.getPlayer(id) ?: continue
            pck.sendPacket(z)
        }
    }

    fun unregister() {
        board.teams.remove(this)
        val pck = WrapperPlayServerScoreboardTeam()
        pck.name = this.name
        pck.mode = 1
        for (id in LitBoard.getBoardHolders(board)) {
            val z = Bukkit.getPlayer(id)
            if (id == null) continue
        }
    }

    enum class NameTagVisibility private constructor(val serialized: String) {

        ALWAYS("always"), HIDE_FOR_OTHER_TEAMS("hideForOtherTeams"), HIDE_FOR_OWN_TEAM("hideForOwnTeam"), NEVER("never");

        fun serialize(): String {
            return serialized
        }
    }

    enum class CollisionRule private constructor(val serialized: String) {

        ALWAYS("always"), PUSH_OTHER_TEAMS("pushOtherTeams"), PUSH_OWN_TEAM("pushOwnTeam"), NEVER("never");

        fun serialize(): String {
            return serialized
        }
    }

}
