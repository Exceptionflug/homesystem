package de.exceptionflug.watertouch

import de.exceptionflug.watertouch.WaterTouchClickableInventoryItem
import de.exceptionflug.watertouch.WaterTouchInventory
import de.exceptionflug.watertouch.WaterTouchInventoryController
import de.exceptionflug.watertouch.WaterTouchInventoryItem
import org.bukkit.Material
import org.bukkit.entity.HumanEntity
import org.bukkit.inventory.ItemStack

import java.util.ArrayList
import java.util.function.BiConsumer

class WaterTouchMultiPageInventory(private val title: String) {
    private val pages = ArrayList<WaterTouchInventory>()
    private var index: Int = 0
    private var currentPage = 1

    init {
        val inv = WaterTouchInventory(6, title + " §7[Seite 1]")
        inv.fillEmptySlots = false
        pages.add(inv)
    }

    fun addItem(item: WaterTouchInventoryItem) {
        if (index > 44) {
            val np = WaterTouchClickableInventoryItem(ItemStack(Material.PAPER))
            np.setDisplayName("§eNächste Seite")
            np.setOnClick(BiConsumer { t, u -> kotlin.run {
                currentPage++
                val p = t.whoClicked
                p.closeInventory()
                p.openInventory(pages[currentPage - 1].asBukkitInv())
            } })
            pages[pages.size - 1].setItem(np, 53)

            pages[pages.size - 1].updateInventory()
            val inv = WaterTouchInventory(6, title + (" §7[Seite " + (pages.size + 1) + "]"))
            inv.fillEmptySlots = false
            val pp = WaterTouchClickableInventoryItem(ItemStack(Material.PAPER))
            pp.setDisplayName("§eVorherige Seite")
            pp.setOnClick(BiConsumer { t, u -> kotlin.run {
                currentPage--
                val p = t.whoClicked
                p.closeInventory()
                p.openInventory(pages[currentPage - 1].asBukkitInv())
            } })
            inv.setItem(pp, 45)
            pages.add(inv)
            index = 0
        }
        val inv = pages[pages.size - 1]
        inv.setItem(item, index)
        index++
        inv.updateInventory()
    }

    fun showUp(e: HumanEntity) {
        e.openInventory(pages[currentPage - 1].asBukkitInv())
    }

    fun reset() {
        for(inv in pages) {
            WaterTouchInventoryController.unregisterInventory(inv)
        }
        pages.clear()
        val inv = WaterTouchInventory(6, title + " §7[Seite 1]")
        inv.fillEmptySlots = false
        pages.add(inv)
        index = 0
        currentPage = 1
    }

}
