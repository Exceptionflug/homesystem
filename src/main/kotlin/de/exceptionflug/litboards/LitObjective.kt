package de.exceptionflug.litboards

import com.comphenix.packetwrapper.WrapperPlayServerScoreboardDisplayObjective
import com.comphenix.packetwrapper.WrapperPlayServerScoreboardObjective
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.scoreboard.DisplaySlot

import java.util.HashSet

class LitObjective internal constructor(val scoreboard: LitBoard, val name: String, val criteria: String) {

    var displayName: String? = null
        set(value) {
            field = value
            val pck = WrapperPlayServerScoreboardObjective()
            pck.name = name
            pck.displayName = displayName
            pck.mode = 2
            pck.healthDisplay = WrapperPlayServerScoreboardObjective.HealthDisplay.INTEGER
            for (id in LitBoard.getBoardHolders(scoreboard)) {
                if (id == null) continue
                val z = Bukkit.getPlayer(id) ?: continue
                pck.sendPacket(z)
            }
        }
    val scores = HashSet<LitScore>()


    val isModifiable: Boolean
        @Throws(IllegalStateException::class)
        get() = true


    var displaySlot: DisplaySlot?
        @Throws(IllegalStateException::class)
        get() {
            for (dsp in scoreboard.slotLitObjectiveMultimap.keySet()) {
                if (dsp == null) continue
                if (scoreboard.slotLitObjectiveMultimap.get(dsp).equals(this)) return dsp
            }
            return null
        }
        @Throws(IllegalStateException::class)
        set(slot) {
            val old = displaySlot
            if (old != null) {
                scoreboard.slotLitObjectiveMultimap.remove(old, this)
            }
            scoreboard.slotLitObjectiveMultimap.put(slot, this)
            val pck = WrapperPlayServerScoreboardDisplayObjective()
            if (displaySlot != null) {
                pck.scoreName = displayName
            } else {
                pck.scoreName = ""
            }
            if (displaySlot == DisplaySlot.PLAYER_LIST) {
                pck.position = 0
            } else if (displaySlot == DisplaySlot.SIDEBAR) {
                pck.position = 1
            } else if (displaySlot == DisplaySlot.BELOW_NAME) {
                pck.position = 2
            }
            for (id in LitBoard.getBoardHolders(scoreboard)) {
                if (id == null) continue
                val z = Bukkit.getPlayer(id) ?: continue
                pck.sendPacket(z)
            }
        }

    init {
        this.displayName = name
    }


    @Throws(IllegalStateException::class)
    fun unregister() {
        val slot = displaySlot
        scoreboard.slotLitObjectiveMultimap.remove(slot, this)
        val pck = WrapperPlayServerScoreboardObjective()
        pck.name = name
        pck.displayName = displayName
        pck.mode = 1
        for (id in LitBoard.getBoardHolders(scoreboard)) {
            if (id == null) continue
            val z = Bukkit.getPlayer(id) ?: continue
            pck.sendPacket(z)
        }
    }


    @Throws(IllegalArgumentException::class, IllegalStateException::class)
    fun getScore(player: OfflinePlayer): LitScore {
        for (score in scores) {
            if (score.player != null && score.player == player) return score
        }
        val out = LitScore(this, player)
        scores.add(out)
        return out
    }


    @Throws(IllegalArgumentException::class, IllegalStateException::class)
    fun getScore(entry: String): LitScore {
        for (score in scores) {
            if (score.entry != null && score.entry == entry) return score
        }
        val out = LitScore(this, entry)
        scores.add(out)
        return out
    }
}
