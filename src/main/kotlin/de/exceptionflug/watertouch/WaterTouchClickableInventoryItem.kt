package de.exceptionflug.watertouch

import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack

import java.util.function.BiConsumer

class WaterTouchClickableInventoryItem(isis: ItemStack) : WaterTouchInventoryItem(isis) {

    private var consumer: BiConsumer<InventoryClickEvent, WaterTouchClickableInventoryItem>? = null

    fun setOnClick(consumer: BiConsumer<InventoryClickEvent, WaterTouchClickableInventoryItem>): WaterTouchClickableInventoryItem {
        this.consumer = consumer
        return this
    }

    fun fire(e: InventoryClickEvent) {
        consumer!!.accept(e, this)
    }

}
