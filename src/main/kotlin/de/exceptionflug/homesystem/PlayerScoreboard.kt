package de.exceptionflug.homesystem

import de.exceptionflug.homesystem.home.Home
import de.exceptionflug.homesystem.home.IOwnerSwap
import de.exceptionflug.litboards.LitBoard
import org.bukkit.Bukkit
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitTask
import org.bukkit.scoreboard.DisplaySlot

class PlayerScoreboard(var player: Player) {

    private val board = LitBoard()
    private val objective = board.registerNewObjective("litobjective", "dummy")
    private val task: BukkitTask
    private var lastNearestHome: Home? = null

    init {
        objective.displayName = "§6Home§eSystem"
        objective.getScore("§6").score = 9
        objective.getScore("§8» §6Anzahl an Häusern").score = 8
        var amount = HomeSystemPlugin.getInstance().homeStorage.getByOwner(player.uniqueId).size
        val scoreHomeAmount = objective.getScore("   §6"+(if(amount == 1) "ein Haus" else "$amount Häuser"))
        scoreHomeAmount.score = 7
        objective.getScore("§4").score = 6
        objective.getScore("§8» §6Anzahl an Täuschen").score = 5
        amount = getSwapsInvolvedTo().size
        val scoreSwapAmount = objective.getScore("   §6"+(if(amount == 1) "ein Tausch" else "$amount Täusche"))
        scoreSwapAmount.score = 4
        objective.getScore("§9").score = 3
        objective.getScore("§8» §6Nahstes Haus").score = 2
        val scoreNearestHome = objective.getScore("   §6"+(if(getNearestHome() != null) getNearestHome()!!.id.toString().substring(0, 8) else "N/A"))
        scoreNearestHome.score = 1
        objective.getScore("§1").score = 0
        task = Bukkit.getScheduler().runTaskTimerAsynchronously(HomeSystemPlugin.getInstance(), {
            amount = HomeSystemPlugin.getInstance().homeStorage.getByOwner(player.uniqueId).size
            scoreHomeAmount.entry = "   §6"+(if(amount == 1) "ein Haus" else "$amount Häuser")
            amount = getSwapsInvolvedTo().size
            scoreSwapAmount.entry = "   §6"+(if(amount == 1) "ein Tausch" else "$amount Täusche")
            scoreNearestHome.entry = "   §6"+(if(getNearestHome() != null) getNearestHome()!!.id.toString().substring(0, 8) else "N/A")
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
        if(nearest != null) {
            // Maybe I should move this to somewhere else
            if(closestDistance <= HomeSystemPlugin.getInstance().notificationRadii && (lastNearestHome == null || lastNearestHome!!.id != nearest.id)) {
                val z = Bukkit.getPlayer(nearest.ownerID)
                if(z != null) {
                    if(nearest.ownerID == player.uniqueId) {
                        z.sendMessage("${HomeSystemPlugin.PREFIX} §6Du §7näherst dich deinem Haus §6${nearest.name}")
                    } else {
                        z.sendMessage("${HomeSystemPlugin.PREFIX} §6${player.name} §7nähert sich deinem Haus §6${nearest.name}")
                    }
                    z.playSound(z.location, Sound.NOTE_PIANO, 1F, 1F)
                }
                lastNearestHome = nearest
            }
            if(closestDistance > HomeSystemPlugin.getInstance().notificationRadii) {
                lastNearestHome = null
            }
        } else {
            lastNearestHome = null
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