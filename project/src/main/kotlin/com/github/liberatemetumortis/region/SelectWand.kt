package com.github.liberatemetumortis.region

import com.github.liberatemetumortis.region.Utils.Companion.translateColors
import org.bukkit.Material
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.Damageable

class SelectWand(val config: FileConfiguration) {
    private val item: ItemStack
    init {
        val section = config.getConfigurationSection("wand")!!
        val material = Material.getMaterial(section.getString("material")!!)
        val name = section.getString("name")!!.translateColors()
        val lore = section.getStringList("lore").map { it.translateColors() }
        val data = section.getInt("data")
        if (material == null) {
            throw Exception("Material not found")
        }
        val itemStack = ItemStack(material, 1)
        val itemMeta = itemStack.itemMeta
        (itemMeta as? Damageable)?.damage = data
        itemMeta?.displayName(name)
        itemMeta?.lore(lore)
        itemStack.itemMeta = itemMeta
        item = itemStack
    }

    fun isWand(itemStack: ItemStack): Boolean {
        return itemStack.isSimilar(item)
    }

    fun giveWand(player: Player) {
        player.inventory.addItem(item)
    }
}
