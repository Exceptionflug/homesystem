package de.exceptionflug.watertouch

import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.HumanEntity
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import java.util.*

class WaterTouchInventory {

    internal val items = HashMap<Int, WaterTouchInventoryItem>()
    private var bukkitInv: Inventory? = null
    var fillEmptySlots = true

    val name: String
        get() = bukkitInv!!.title

    constructor(invis: Inventory) {
        this.bukkitInv = invis
        WaterTouchInventoryController.registerInventory(this)
    }

    constructor(rows: Int, name: String) {
        this.bukkitInv = Bukkit.createInventory(null, rows * 9, name)
        WaterTouchInventoryController.registerInventory(this)
    }

    fun addItem(item: WaterTouchInventoryItem) {
        items.put(items.size, item)
    }

    fun setItem(item: WaterTouchInventoryItem, slot: Int) {
        items.put(slot, item)
    }

    fun getItem(slot: Int): WaterTouchInventoryItem? {
        return items.get(slot)
    }

    fun asBukkitInv(): Inventory {
        bukkitInv!!.clear()
        for (i in 0 until bukkitInv!!.size) {
            bukkitInv!!.setItem(i, (items as java.util.Map<Int, WaterTouchInventoryItem>).getOrDefault(i, WaterTouchInventoryItem(if (fillEmptySlots) ItemStack(Material.STAINED_GLASS_PANE, 1, 7.toByte().toShort()) else ItemStack(Material.AIR)).setDisplayName("§7")).stack)
        }
        return bukkitInv!!
    }

    fun isOpenedByPlayer(e: HumanEntity): Boolean {
        return bukkitInv!!.viewers.contains(e)
    }

    fun updateInventory() {
        for (he in bukkitInv!!.viewers) {
            he.openInventory(asBukkitInv())
        }
    }

    override fun equals(obj: Any?): Boolean {
        if (obj is WaterTouchInventory) {
            val winv = obj as WaterTouchInventory?
            return if (winv!!.bukkitInv == null || bukkitInv == null) {
                winv.name == name
            } else winv.bukkitInv == bukkitInv
        } else {
            return false
        }
    }

}
