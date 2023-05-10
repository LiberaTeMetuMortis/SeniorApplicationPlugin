package com.github.liberatemetumortis.region.gui

import com.github.liberatemetumortis.region.Utils.Companion.translateColors
import org.bukkit.Material
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.inventory.ItemStack

abstract class PageUtils {
    companion object {
        const val REGION_PER_PAGE = 28
        val FILLER_SLOTS = listOf(
            0, 1, 2, 3, 4, 5, 6, 7, 8,
            9, 17,
            18, 26,
            27, 35,
            36, 44,
            46, 47, 48, 49, 50, 51, 52
        )
        val USABLE_SLOTS = listOf(
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34,
            37, 38, 39, 40, 41, 42, 43
        )
        fun getItem(path: String, configurationSection: ConfigurationSection): ItemStack {
            val itemMaterial = configurationSection.getString("$path.material")
            val itemName = configurationSection.getString("$path.name")
            val itemLore = configurationSection.getStringList("$path.lore")
            val itemStack = ItemStack(Material.getMaterial(itemMaterial!!)!!)
            val itemMeta = itemStack.itemMeta
            itemMeta.displayName(itemName.translateColors())
            itemMeta.lore(itemLore.map { it.translateColors() })
            itemStack.itemMeta = itemMeta
            return itemStack
        }
    }
}
