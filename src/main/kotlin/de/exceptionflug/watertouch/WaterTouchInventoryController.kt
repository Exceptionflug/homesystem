package de.exceptionflug.watertouch

import de.exceptionflug.homesystem.HomeSystemPlugin
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.Inventory

import java.util.HashSet

class WaterTouchInventoryController : Listener {
    init {
        Bukkit.getPluginManager().registerEvents(this, HomeSystemPlugin.getInstance())
    }

    private fun getWaterTouchInventory(inventory: Inventory?): WaterTouchInventory? {
        if (inventory == null) return null
        for (inv in inventoryHashSet) {
            if (inventory == inv.asBukkitInv()) {
                return inv
            }
        }
        return null
    }

    @EventHandler
    fun onClick(e: InventoryClickEvent) {
        val inv = getWaterTouchInventory(e.clickedInventory)
        if (inv != null) {
            if (inv.isOpenedByPlayer(e.whoClicked)) {
                e.isCancelled = true
                val slot = e.slot
                val item = inv.getItem(slot)
                if (item != null) {
                    (item as? WaterTouchClickableInventoryItem)?.fire(e)
                }
            }
        }
    }

    companion object {

        private val inventoryHashSet = HashSet<WaterTouchInventory>()

        fun registerInventory(inv: WaterTouchInventory) {
            inventoryHashSet.add(inv)
        }

        fun unregisterInventory(inv: WaterTouchInventory) {
            inventoryHashSet.remove(inv)
        }
    }

}
