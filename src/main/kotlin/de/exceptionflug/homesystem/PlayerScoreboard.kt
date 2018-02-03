package de.exceptionflug.homesystem

import de.exceptionflug.homesystem.home.Home
import de.exceptionflug.homesystem.home.IOwnerSwap
import de.exceptionflug.litboards.LitBoard
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitTask
import org.bukkit.scoreboard.DisplaySlot

class PlayerScoreboard(var player: Player) {

    private val board = LitBoard()
    private val objective = board.registerNewObjective("litobjective", "dummy")
    private val task: BukkitTask

    init {
        objective.displayName = "§6Home§eSystem"
        objective.getScore("§6").score = 9
        objective.getScore("§8» §6Anzahl an Häusern").score = 8
        val scoreHomeAmount = objective.getScore("  §6${HomeSystemPlugin.getInstance().homeStorage.getByOwner(player.uniqueId).size} Häuser")
        scoreHomeAmount.score = 7
        objective.getScore("§4").score = 6
        objective.getScore("§8» §6Anzahl an Täuschen").score = 5
        val scoreSwapAmount = objective.getScore("  §6${getSwapsInvolvedTo().size} Täusche")
        scoreSwapAmount.score = 4
        objective.getScore("§9").score = 3
        objective.getScore("§8» §6Nahstes Haus").score = 2
        val scoreNearestHome = objective.getScore("  §6"+(if(getNearestHome() != null) getNearestHome()!!.id.toString().substring(0, 8) else "N/A"))
        scoreNearestHome.score = 1

        task = Bukkit.getScheduler().runTaskTimerAsynchronously(HomeSystemPlugin.getInstance(), {
            scoreHomeAmount.entry = "  §6${HomeSystemPlugin.getInstance().homeStorage.getByOwner(player.uniqueId).size} Häuser"
            scoreSwapAmount.entry = "  §6${getSwapsInvolvedTo().size} Täusche"
            scoreNearestHome.entry = "  §6"+(if(getNearestHome() != null) getNearestHome()!!.id.toString().substring(0, 8) else "N/A")
        }, 10, 10)
        objective.displaySlot = DisplaySlot.SIDEBAR
        LitBoard.sendToPlayer(player, board)
    }

    private fun getNearestHome(): Home? {
        var nearest: Home? = null
        var closestDistance = Double.MAX_VALUE
        for(home in HomeSystemPlugin.getInstance().homeStorage.getAll()) {
            if(home.location.distance(player.location) < closestDistance && home.location.world.uid == player.location.world.uid) {
                closestDistance = home.location.distance(player.location)
                nearest = home
            }
        }
        return nearest
    }

    private fun getSwapsInvolvedTo(): Set<IOwnerSwap> {
        val allSwaps = HomeSystemPlugin.getInstance().homeStorage.getAllSwaps()
        val involvedTo = HashSet<IOwnerSwap>()
        for(swap in allSwaps) {
            val home1 = swap.getHome1()
            val home2 = swap.getHome2()
            if(home1 == null || home2 == null) continue
            if(home1.ownerID == player.uniqueId || home2.ownerID == player.uniqueId) involvedTo.add(swap)
        }
        return involvedTo
    }

    fun stop() {
        task.cancel()
    }

}