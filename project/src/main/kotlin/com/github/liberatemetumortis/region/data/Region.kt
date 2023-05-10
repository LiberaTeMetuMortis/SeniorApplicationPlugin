package com.github.liberatemetumortis.region.data

import com.github.liberatemetumortis.region.Utils.Companion.translateColors
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.inventory.ItemStack
import java.util.*
import kotlin.collections.ArrayList

data class Region(var name: String, val owner: String, var area: Area) {
    val whitelisteds: MutableList<String> = mutableListOf()
    var id: Int = 0
    companion object {
        val regions = ArrayList<Region>()
    }
    init {
        regions.add(this)
    }
    fun toItem(configurationSection: ConfigurationSection): ItemStack {
        val material = configurationSection.getString("material")
        val name = replacePlaceholders(configurationSection.getString("name")!!)
        val lore = configurationSection.getStringList("lore").map(::replacePlaceholders)
        val itemStack = ItemStack(Material.getMaterial(material!!)!!)
        val itemMeta = itemStack.itemMeta
        itemMeta.displayName(name.translateColors())
        itemMeta.lore(lore.map { it.translateColors() })
        itemStack.itemMeta = itemMeta
        return itemStack
    }
    fun replacePlaceholders(string: String): String {
        return string
            .replace("%owner%", Bukkit.getOfflinePlayer(UUID.fromString(owner)).name!!)
            .replace("%region%", name)
            .replace("%whitelisted%", whitelisteds.mapNotNull { Bukkit.getOfflinePlayer(UUID.fromString(it)).name }.joinToString(", "))
    }
}
