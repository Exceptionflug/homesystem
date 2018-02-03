package de.exceptionflug.litboards

import com.comphenix.packetwrapper.WrapperPlayServerScoreboardDisplayObjective
import com.comphenix.packetwrapper.WrapperPlayServerScoreboardObjective
import com.comphenix.packetwrapper.WrapperPlayServerScoreboardScore
import com.comphenix.packetwrapper.WrapperPlayServerScoreboardTeam
import com.comphenix.protocol.PacketType
import com.comphenix.protocol.ProtocolLibrary
import com.comphenix.protocol.events.PacketContainer
import com.comphenix.protocol.wrappers.EnumWrappers
import com.google.common.collect.ArrayListMultimap
import com.google.common.collect.Multimap
import de.exceptionflug.homesystem.utils.ReflectionUtil
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import org.bukkit.scoreboard.*

import java.lang.reflect.InvocationTargetException
import java.util.*

class LitBoard {

    val slotLitObjectiveMultimap: Multimap<DisplaySlot, LitObjective> = ArrayListMultimap.create()
    val teams = HashSet<LitTeam>()


    val objectives: Set<LitObjective>
        get() {
            val out = HashSet<LitObjective>()
            for (dsp in slotLitObjectiveMultimap.keySet()) {
                for (objective in slotLitObjectiveMultimap.get(dsp)) {
                    out.add(objective)
                }
            }
            return out
        }


    val players: Set<OfflinePlayer>
        get() {
            val out = HashSet<OfflinePlayer>()
            for (dsp in slotLitObjectiveMultimap.keySet()) {
                for (objective in slotLitObjectiveMultimap.get(dsp)) {
                    for (score in objective.scores) {
                        if (score.player != null) out.add(score.player!!)
                    }
                }
            }
            return out
        }


    val entries: Set<String>
        get() {
            val out = HashSet<String>()
            for (dsp in slotLitObjectiveMultimap.keySet()) {
                for (objective in slotLitObjectiveMultimap.get(dsp)) {
                    for (score in objective.scores) {
                        if (score.entry != null) out.add(score.entry!!)
                    }
                }
            }
            return out
        }

    fun registerNewObjective(name: String, criteria: String): LitObjective {
        val objective = LitObjective(this, name, criteria)
        slotLitObjectiveMultimap.put(null, objective)
        val pck = WrapperPlayServerScoreboardObjective()
        pck.displayName = name
        pck.name = name
        pck.mode = 0
        for (id in getBoardHolders(this)) {
            if (id == null) continue
            val z = Bukkit.getPlayer(id) ?: continue
            pck.sendPacket(z)
        }
        return objective
    }

    fun getObjective(name: String): LitObjective? {
        for (dsp in slotLitObjectiveMultimap.keySet()) {
            for (objective in slotLitObjectiveMultimap.get(dsp)) {
                if (objective.name == name) return objective
            }
        }
        return null
    }


    fun getObjectivesByCriteria(criteria: String): Set<LitObjective> {
        val out = HashSet<LitObjective>()
        for (dsp in slotLitObjectiveMultimap.keySet()) {
            for (objective in slotLitObjectiveMultimap.get(dsp)) {
                if (objective.criteria == criteria) out.add(objective)
            }
        }
        return out
    }


    fun getObjective(slot: DisplaySlot): Objective {
        return (slotLitObjectiveMultimap.get(slot) as List<*>)[0] as Objective
    }


    fun getScores(player: OfflinePlayer): Set<LitScore> {
        val out = HashSet<LitScore>()
        for (dsp in slotLitObjectiveMultimap.keySet()) {
            for (objective in slotLitObjectiveMultimap.get(dsp)) {
                val s = objective.getScore(player)
                if (s != null) out.add(s)
            }
        }
        return out
    }


    fun getScores(entry: String): Set<LitScore> {
        val out = HashSet<LitScore>()
        for (dsp in slotLitObjectiveMultimap.keySet()) {
            for (objective in slotLitObjectiveMultimap.get(dsp)) {
                val s = objective.getScore(entry)
                if (s != null) out.add(s)
            }
        }
        return out
    }


    fun resetScores(player: OfflinePlayer) {
        for (s in getScores(player)) {
            s.reset()
        }
    }


    fun resetScores(entry: String) {
        for (s in getScores(entry)) {
            s.reset()
        }
    }

    fun getTeam(teamName: String): LitTeam? {
        for (team in teams) {
            if (team.name == teamName) return team
        }
        return null
    }


    fun registerNewTeam(name: String): LitTeam {
        val t = LitTeam(this, name)
        teams.add(t)
        val pck = WrapperPlayServerScoreboardTeam()
        pck.name = name
        pck.mode = 0
        pck.displayName = name
        pck.prefix = ""
        pck.suffix = ""
        pck.packOptionData = 1
        pck.nameTagVisibility = t.nameTagVisibility.serialize()
        if (!ReflectionUtil.version.startsWith("v1_8")) {
            pck.collisionRule = t.collisionRule.serialize()
        }
        pck.color = -1
        pck.setPlayers(ArrayList())
        for (id in getBoardHolders(this)) {
            if (id == null) continue
            val z = Bukkit.getPlayer(id) ?: continue
            pck.sendPacket(z)
        }
        return t
    }


    fun clearSlot(slot: DisplaySlot) {
        for (objective in slotLitObjectiveMultimap.get(slot)) {
            objective.displaySlot = null
        }
    }

    companion object {

        private val boardHashMap = HashMap<UUID, LitBoard>()

        fun sendToPlayer(p: Player, board: LitBoard?) {
            val oldBoard = boardHashMap[p.uniqueId]
            if (oldBoard != null) {
                for (team in oldBoard.teams) {
                    val pck = WrapperPlayServerScoreboardTeam()
                    pck.name = team.name
                    pck.mode = 1
                    pck.sendPacket(p)
                }
                for (objective in oldBoard.objectives) {
                    val pck = WrapperPlayServerScoreboardObjective()
                    pck.mode = 1
                    pck.name = objective.name
                    pck.displayName = objective.displayName
                    pck.sendPacket(p)
                }
            }
            if (board != null) {
                boardHashMap.put(p.uniqueId, board)
                for (objective in board.objectives) {
                    val pck = WrapperPlayServerScoreboardObjective()
                    pck.mode = 0
                    pck.name = objective.name
                    pck.displayName = objective.displayName
                    pck.healthDisplay = WrapperPlayServerScoreboardObjective.HealthDisplay.INTEGER
                    pck.sendPacket(p)
                    for (score in objective.scores) {
                        if (score.isScoreSet == false) continue
                        val pck1 = WrapperPlayServerScoreboardScore()
                        pck1.scoreName = if (score.player != null) score.player!!.name else score.entry
                        pck1.value = score.score
                        pck1.setScoreboardAction(EnumWrappers.ScoreboardAction.CHANGE)
                        pck1.objectiveName = objective.name
                        pck1.sendPacket(p)
                    }
                }
                for (dsp in board.slotLitObjectiveMultimap.keySet()) {
                    if (dsp != null) {
                        for (obj in board.slotLitObjectiveMultimap.get(dsp)) {
                            val pck = WrapperPlayServerScoreboardDisplayObjective()
                            if (dsp == DisplaySlot.SIDEBAR) {
                                pck.position = 1
                            } else if (dsp == DisplaySlot.BELOW_NAME) {
                                pck.position = 2
                            } else {
                                pck.position = 0
                            }
                            pck.scoreName = obj.name
                            pck.sendPacket(p)
                        }
                    }
                }
                for (team in board.teams) {
                    if (ReflectionUtil.version.startsWith("v1_8")) {
                        val pck = PacketContainer(PacketType.Play.Server.SCOREBOARD_TEAM)
                        pck.strings.write(0, team.name)
                        pck.strings.write(1, team.displayName)
                        pck.strings.write(2, team.prefix)
                        pck.strings.write(3, team.suffix)
                        pck.strings.write(4, team.nameTagVisibility.serialize())
                        pck.integers.write(0, team.color)
                        pck.integers.write(1, 0)
                        pck.integers.write(2, if (team.isFriendlyFire) 0x01 else 0x02)
                        pck.getSpecificModifier(Collection::class.java).write(0, team.members)
                        try {
                            ProtocolLibrary.getProtocolManager().sendServerPacket(p, pck)
                        } catch (e: InvocationTargetException) {
                            e.printStackTrace()
                        }

                    } else {
                        val pck = WrapperPlayServerScoreboardTeam()
                        pck.name = team.name
                        pck.mode = 0
                        pck.displayName = team.displayName
                        pck.prefix = team.prefix
                        pck.suffix = team.suffix
                        pck.packOptionData = if (team.isFriendlyFire) 0x01 else 0x02
                        pck.nameTagVisibility = team.nameTagVisibility.serialize()
                        pck.collisionRule = team.collisionRule.serialize()
                        pck.color = team.color
                        pck.players = team.members
                        pck.sendPacket(p)
                    }
                }
            }
        }

        fun getBoardHolders(board: LitBoard): Set<UUID> {
            val out = HashSet<UUID>()
            for (uuid in boardHashMap.keys) {
                if (boardHashMap[uuid] == board) {
                    out.add(uuid)
                }
            }
            return out
        }

        fun getBoardFromPlayer(p: Player): LitBoard? {
            for (uuid in boardHashMap.keys) {
                if (uuid == p.uniqueId) {
                    return boardHashMap[uuid]
                }
            }
            return null
        }
    }

}
