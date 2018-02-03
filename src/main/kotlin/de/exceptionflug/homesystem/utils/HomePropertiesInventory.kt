package de.exceptionflug.homesystem.utils

import de.exceptionflug.homesystem.HomeSystemPlugin
import de.exceptionflug.homesystem.home.Home
import de.exceptionflug.watertouch.WaterTouchClickableInventoryItem
import de.exceptionflug.watertouch.WaterTouchInventory
import de.exceptionflug.watertouch.WaterTouchInventoryItem
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.util.*
import java.util.function.BiConsumer

object HomePropertiesInventory {

    private val mapHomeToGUI = HashMap<UUID, PropertiesGUI>()

    fun open(player: Player, home: Home) {
        if(mapHomeToGUI.containsKey(home.id)) {
            mapHomeToGUI[home.id]!!.open(player)
            return
        }
        val gui = PropertiesGUI(home.id)
        gui.open(player)
        mapHomeToGUI[home.id] = gui
    }

    class PropertiesGUI(val id: UUID) {

        private val inventory = WaterTouchInventory(1, HomeSystemPlugin.getInstance().homeStorage.load(id)!!.name)

        init {
            val delete = WaterTouchClickableInventoryItem(ItemStack(Material.BARRIER)).setDisplayName("§cLöschen") as WaterTouchClickableInventoryItem
            delete.setOnClick(BiConsumer { t, u ->
                run {
                    val obj = HomeSystemPlugin.getInstance().homeStorage.load(id)
                    if(obj != null) {
                        HomeSystemPlugin.getInstance().homeStorage.delete(obj)
                        t.whoClicked.closeInventory()
                        t.whoClicked.sendMessage("${HomeSystemPlugin.PREFIX} §aHaus gelöscht.")
                    } else {
                        t.whoClicked.sendMessage("${HomeSystemPlugin.PREFIX} §cKonnte Haus nicht finden")
                    }
                }
            })
            inventory.setItem(delete, 8)
        }

        fun open(player: Player) {
            val home = HomeSystemPlugin.getInstance().homeStorage.load(id)
            if(home == null) {
                player.sendMessage("${HomeSystemPlugin.PREFIX} §cDas Haus existiert nicht mehr!")
                return
            }
            inventory.setItem(WaterTouchInventoryItem(ItemStack(Material.LAPIS_BLOCK)).setDisplayName("§9Position").addLoreLine("§6${home.location.blockX} ${home.location.blockY} ${home.location.blockZ}").addLoreLine("§7auf Welt §6${home.location.world.name}"), 0)
            val memberNames = ArrayList<String>()
            home.members
                    .map { Bukkit.getOfflinePlayer(it) }
                    .filter { it.hasPlayedBefore() }
                    .mapTo(memberNames) { it.name }
            inventory.setItem(WaterTouchInventoryItem(ItemStack(Material.REDSTONE_BLOCK)).setDisplayName("§cMitglieder").addLoreLine("§6${memberNames.toString().replace("[", "").replace("]", "")}"), 1)
            inventory.setItem(WaterTouchInventoryItem(ItemStack(Material.EMERALD_BLOCK)).setDisplayName("§aEindeutige ID").addLoreLine("§6${home.id}"), 2)
            player.playSound(player.location, Sound.CLICK, 1F, 1F)
            player.openInventory(inventory.asBukkitInv())
        }

    }

}