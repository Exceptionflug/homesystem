package de.exceptionflug.litboards

import de.exceptionflug.homesystem.HomeSystemPlugin
import org.bukkit.Bukkit
import org.bukkit.scheduler.BukkitTask
import org.bukkit.scoreboard.DisplaySlot

class LitSideboardDesigner(name: String) {

    val litBoard: LitBoard
    val sidebar: LitObjective
    private var animatorTask: BukkitTask? = null

    init {
        litBoard = LitBoard()
        sidebar = litBoard.registerNewObjective(name, "dummy")
        sidebar.displaySlot = DisplaySlot.SIDEBAR
    }

    fun animateDisplayName(names: List<String>, delay: Int): LitSideboardDesigner {
        if (animatorTask != null) animatorTask!!.cancel()
        animatorTask = Bukkit.getScheduler().runTaskTimer(HomeSystemPlugin.getInstance(), object : Runnable {

            private var index = 0

            override fun run() {
                sidebar.displayName = names[index]
                index++
                if(index == names.size) index = 0
            }

        }, delay.toLong(), delay.toLong())
        return this
    }

}
