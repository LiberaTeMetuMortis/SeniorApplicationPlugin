package com.github.liberatemetumortis.region.gui.edit

import com.github.liberatemetumortis.region.Utils.Companion.translateColors
import com.github.liberatemetumortis.region.data.Region
import com.github.liberatemetumortis.region.gui.PageUtils
import org.bukkit.Bukkit
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder

class EditPage(private val player: Player, val region: Region, private val configurationSection: ConfigurationSection) : InventoryHolder {
    private val title = configurationSection.getString("title")
    private val inventory: Inventory = Bukkit.createInventory(this, 45, title.translateColors())
    override fun getInventory() = inventory
    companion object {
        val operationMap = mutableMapOf<Player, Pair<Region, Operations>>()
    }
    init {
        val fillerItem = PageUtils.getItem("filler", configurationSection)
        for (slot in 0 until inventory.size) {
            inventory.setItem(slot, fillerItem)
        }
        val renameItem = PageUtils.getItem("rename", configurationSection)
        val renameSlot = configurationSection.getInt("rename.slot")
        val resizeItem = PageUtils.getItem("resize", configurationSection)
        val resizeSlot = configurationSection.getInt("resize.slot")
        val whitelistAddItem = PageUtils.getItem("whitelist_add", configurationSection)
        val whitelistAddSlot = configurationSection.getInt("whitelist_add.slot")
        val whitelistRemoveItem = PageUtils.getItem("whitelist_remove", configurationSection)
        val whitelistRemoveSlot = configurationSection.getInt("whitelist_remove.slot")

        inventory.setItem(renameSlot, renameItem)
        inventory.setItem(resizeSlot, resizeItem)
        inventory.setItem(whitelistAddSlot, whitelistAddItem)
        inventory.setItem(whitelistRemoveSlot, whitelistRemoveItem)
    }

    fun getOperationByClickedSlot(slot: Int): Operations? {
        if (slot == configurationSection.getInt("rename.slot")) return Operations.RENAME
        if (slot == configurationSection.getInt("resize.slot")) return Operations.RESIZE
        if (slot == configurationSection.getInt("whitelist_add.slot")) return Operations.WHITELIST_ADD
        if (slot == configurationSection.getInt("whitelist_remove.slot")) return Operations.WHITELIST_REMOVE
        return null
    }

    fun openGUI() {
        player.openInventory(inventory)
    }
}
