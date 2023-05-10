package com.github.liberatemetumortis.region.gui.regions

import com.github.liberatemetumortis.region.Utils.Companion.translateColors
import com.github.liberatemetumortis.region.data.Region
import com.github.liberatemetumortis.region.gui.PageUtils
import org.bukkit.Bukkit
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder

class RegionPage(title: String, val creater: RegionPageCreater, private val regions: List<Region>) : InventoryHolder {
    private val inventory: Inventory = Bukkit.createInventory(this, 54, title.translateColors())
    override fun getInventory() = inventory

    val regionMap = mutableMapOf<Int, Region>()
    init {
        for (i in regions.indices) {
            val item = regions[i].toItem(creater.configurationSection.getConfigurationSection("entry")!!)
            inventory.setItem(PageUtils.USABLE_SLOTS[i], item)
            regionMap[PageUtils.USABLE_SLOTS[i]] = regions[i]
        }
    }

    fun getRegionByClickedSlot(slot: Int): Region? {
        return regionMap[slot]
    }
}
