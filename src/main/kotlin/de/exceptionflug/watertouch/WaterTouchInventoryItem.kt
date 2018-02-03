package de.nanox.nnxcore.spigot.watertouch

import org.bukkit.Bukkit
import org.bukkit.Color
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.inventory.meta.LeatherArmorMeta
import org.bukkit.inventory.meta.SkullMeta

open class WaterTouchInventoryItem(val stack: ItemStack) {

    fun setDisplayName(name: String): WaterTouchInventoryItem {
        if(!stack.hasItemMeta()) {
            stack.itemMeta = Bukkit.getItemFactory().getItemMeta(stack.type)
        }
        var meta = stack.itemMeta
        if(meta == null) {
            meta = Bukkit.getItemFactory().getItemMeta(stack.type)
            if(meta == null) {
                return this
            }
        }
        meta!!.displayName = name
        stack.itemMeta = meta
        return this
    }

    fun setEnchanted(ench: Boolean): WaterTouchInventoryItem {
        val meta = stack.itemMeta
        if (ench) {
            meta.addEnchant(Enchantment.ARROW_DAMAGE, 1, true)
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS)
        } else {
            for (e in meta.enchants.keys) {
                meta.removeEnchant(e)
            }
        }
        stack.itemMeta = meta
        return this
    }

    fun addItemFlags(vararg flags: ItemFlag): WaterTouchInventoryItem {
        val meta = stack.itemMeta
        meta.addItemFlags(*flags)
        stack.itemMeta = meta
        return this
    }

    fun delItemFlags(vararg flags: ItemFlag): WaterTouchInventoryItem {
        val meta = stack.itemMeta
        meta.removeItemFlags(*flags)
        stack.itemMeta = meta
        return this
    }

    fun addEnchant(ench: Enchantment, lvl: Int, override: Boolean): WaterTouchInventoryItem {
        val meta = stack.itemMeta
        meta.addEnchant(ench, lvl, override)
        stack.itemMeta = meta
        return this
    }

    fun delEnchant(ench: Enchantment): WaterTouchInventoryItem {
        val meta = stack.itemMeta
        meta.removeEnchant(ench)
        stack.itemMeta = meta
        return this
    }

    fun addLoreLine(line: String): WaterTouchInventoryItem {
        if(!stack.hasItemMeta()) {
            stack.itemMeta = Bukkit.getItemFactory().getItemMeta(stack.type)
        }
        var meta = stack.itemMeta
        if(meta == null) {
            meta = Bukkit.getItemFactory().getItemMeta(stack.type)
            if(meta == null) {
                return this
            }
        }
        if(meta.lore == null) {
            val newList = ArrayList<String>()
            newList.add(line)
            meta.lore = newList
        } else {
            val oldList = meta.lore
            oldList.add(line)
            meta.lore = oldList
        }
        stack.itemMeta = meta
        return this
    }

    fun setSkullOwner(username: String?): WaterTouchInventoryItem {
        var meta = stack.itemMeta as SkullMeta
        meta.owner = username
        stack.itemMeta = meta
        return this
    }

    fun setColor(color: Color): WaterTouchInventoryItem {
        var meta = stack.itemMeta as LeatherArmorMeta
        meta.color = color
        stack.itemMeta = meta
        return this
    }

}
