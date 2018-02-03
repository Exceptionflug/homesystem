package de.exceptionflug.litboards

import com.comphenix.packetwrapper.WrapperPlayServerScoreboardScore
import com.comphenix.protocol.wrappers.EnumWrappers
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer

class LitScore {

    var player: OfflinePlayer? = null
        set(value) {
            val flag = isScoreSet
            reset()
            field = value
            if (flag) {
                score = this.score
            }
        }
    var entry: String? = null
        set(value) {
            val flag = isScoreSet
            reset()
            field = value
            if (flag) {
                score = this.score
            }
        }
    val objective: LitObjective

    var score: Int = 0
        set(score) {
            field = score
            isScoreSet = true
            val pck = WrapperPlayServerScoreboardScore()
            pck.objectiveName = objective.name
            pck.setScoreboardAction(EnumWrappers.ScoreboardAction.CHANGE)
            pck.scoreName = if (player != null) player!!.name else entry
            pck.value = score
            for (id in LitBoard.getBoardHolders(scoreboard)) {
                if (id == null) continue
                val z = Bukkit.getPlayer(id) ?: continue
                pck.sendPacket(z)
            }
        }

    var isScoreSet: Boolean = false
        private set

    val scoreboard: LitBoard
        get() = objective.scoreboard

    internal constructor(objective: LitObjective, entry: String) {
        this.entry = entry
        this.objective = objective
    }

    internal constructor(objective: LitObjective, offlinePlayer: OfflinePlayer) {
        this.player = offlinePlayer
        this.objective = objective
    }

    fun reset() {
        if (isScoreSet == false) return
        isScoreSet = false
        val pck = WrapperPlayServerScoreboardScore()
        pck.objectiveName = objective.name
        pck.setScoreboardAction(EnumWrappers.ScoreboardAction.REMOVE)
        pck.scoreName = if (player != null) player!!.name else entry
        for (id in LitBoard.getBoardHolders(scoreboard)) {
            if (id == null) continue
            val z = Bukkit.getPlayer(id) ?: continue
            pck.sendPacket(z)
        }
    }

}
