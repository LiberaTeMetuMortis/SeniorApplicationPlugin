package com.github.liberatemetumortis.region

import com.github.liberatemetumortis.region.Main.Companion.firstLocationMap
import com.github.liberatemetumortis.region.Main.Companion.selectedAreaMap
import com.github.liberatemetumortis.region.Utils.Companion.stripColors
import com.github.liberatemetumortis.region.Utils.Companion.translateColors
import com.github.liberatemetumortis.region.data.Area
import com.github.liberatemetumortis.region.data.Region.Companion.regions
import com.github.liberatemetumortis.region.gui.edit.EditPage
import com.github.liberatemetumortis.region.gui.edit.EditPage.Companion.operationMap
import com.github.liberatemetumortis.region.gui.edit.Operations
import com.github.liberatemetumortis.region.gui.regions.RegionPage
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.player.AsyncPlayerChatEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerItemHeldEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.inventory.EquipmentSlot

class EventListener(val plugin: Main) : Listener {
    init {
        plugin.server.pluginManager.registerEvents(this, plugin)
    }

    @EventHandler
    fun onItemHeld(event: PlayerItemHeldEvent) {
        firstLocationMap.remove(event.player)
        selectedAreaMap.remove(event.player)
    }

    @EventHandler
    fun selectEvent(event: PlayerInteractEvent) {
        // Check for right hand click
        if (event.hand != EquipmentSlot.HAND) return
        if (plugin.selectWand.isWand(event.player.inventory.itemInMainHand)) {
            if (event.clickedBlock == null) return
            val messagesSection = plugin.config.getConfigurationSection("messages")!!
            if (!operationMap.containsKey(event.player)) {
                if (regions.any { event.clickedBlock!!.location in it.area }) {
                    event.player.sendMessage(messagesSection.getString("cant-select").translateColors())
                    return
                }
            } else {
                val (region, operation) = operationMap[event.player]!!
                if (regions.any { event.clickedBlock!!.location in it.area && it.id != region.id }) {
                    event.player.sendMessage(messagesSection.getString("cant-select").translateColors())
                    return
                }
            }

            if (event.clickedBlock!!.type == Material.AIR) return
            event.isCancelled = true
            if (firstLocationMap.containsKey(event.player)) {
                val firstLocation = firstLocationMap[event.player]!!
                val secondLocation = event.clickedBlock!!.location
                val area = Area.from(firstLocation, secondLocation)
                if (area == null) { // Check for if first and second locations are in the same world
                    firstLocationMap[event.player] = secondLocation
                    return
                }
                if (regions.any { it.area.intersects(area) }) {
                    event.player.sendMessage(messagesSection.getString("area-intersects").translateColors())
                    return
                }

                if (operationMap.containsKey(event.player) && operationMap[event.player]!!.second == Operations.RESIZE) {
                    val (region) = operationMap[event.player]!!
                    region.area = area
                    event.player.sendMessage(messagesSection.getString("resized").translateColors())
                } else {
                    selectedAreaMap[event.player] = area
                    event.player.sendMessage(messagesSection.getString("second-pos-selected").translateColors())
                }

                firstLocationMap.remove(event.player)
            } else {
                firstLocationMap[event.player] = event.clickedBlock!!.location
                event.player.sendMessage(messagesSection.getString("first-pos-selected").translateColors())
            }
        }
    }

    @EventHandler
    fun onPlayerInteract(event: PlayerInteractEvent) {
        // Check for right hand click
        if (event.hand != EquipmentSlot.HAND) return
        event.clickedBlock ?: return
        val region = regions.find { event.clickedBlock!!.location in it.area } ?: return
        val uuidOfPlayer = event.player.uniqueId.toString()
        if (region.owner != uuidOfPlayer && uuidOfPlayer !in region.whitelisteds && event.player.hasPermission("region.bypass")) {
            event.isCancelled = true
            event.player.sendMessage(plugin.config.getString("no-access").translateColors())
        }
    }

    @EventHandler
    fun onRegionSelect(event: InventoryClickEvent) {
        if (event.inventory.holder !is RegionPage) return
        if (event.whoClicked !is Player) return
        val player = event.whoClicked as Player
        event.isCancelled = true
        val regionPage = event.inventory.holder as RegionPage
        when (event.slot) {
            45 -> {
                regionPage.creater.previousPage()
                return
            }
            53 -> {
                regionPage.creater.nextPage()
                return
            }
            else -> {
                val region = regionPage.getRegionByClickedSlot(event.slot) ?: return
                EditPage(player, region, plugin.config.getConfigurationSection("gui.edit")!!).openGUI()
            }
        }
    }

    @EventHandler
    fun onOperationSelect(event: InventoryClickEvent) {
        if (event.inventory.holder !is EditPage) return
        if (event.whoClicked !is Player) return
        val player = event.whoClicked as Player
        event.isCancelled = true
        val editPage = event.inventory.holder as EditPage
        val operation = editPage.getOperationByClickedSlot(event.slot) ?: return
        operationMap[player] = editPage.region to operation
        val messagesSection = plugin.config.getConfigurationSection("messages")!!
        player.closeInventory()
        when (operation) {
            Operations.RENAME -> {
                player.sendMessage(messagesSection.getString("rename").translateColors())
                return
            }
            Operations.RESIZE -> {
                player.sendMessage(messagesSection.getString("resize").translateColors())
                return
            }
            Operations.WHITELIST_ADD -> {
                player.sendMessage(messagesSection.getString("whitelist_add").translateColors())
                return
            }
            Operations.WHITELIST_REMOVE -> {
                player.sendMessage(messagesSection.getString("whitelist_remove").translateColors())
                return
            }
        }
    }

    @EventHandler
    fun onChatMessage(event: AsyncPlayerChatEvent) {
        if (!operationMap.containsKey(event.player)) return
        val (region, operation) = operationMap[event.player]!!
        val firstArgument = event.message.split(" ")[0]
        val messagesSection = plugin.config.getConfigurationSection("messages")!!
        event.isCancelled = true
        if (firstArgument == "cancel") {
            event.player.sendMessage(messagesSection.getString("cancelled").translateColors())
            operationMap.remove(event.player)
            return
        }
        when (operation) {
            Operations.RENAME -> {
                region.name = firstArgument.stripColors()
                event.player.sendMessage(messagesSection.getString("renamed").translateColors())
            }
            Operations.WHITELIST_ADD -> {
                val selectedPlayer = Bukkit.getOfflinePlayer(firstArgument)
                val uuid = selectedPlayer.uniqueId.toString()
                region.whitelisteds.add(uuid)
                println(uuid)
                event.player.sendMessage(messagesSection.getString("whitelist-added").translateColors())
            }
            Operations.WHITELIST_REMOVE -> {
                val selectedPlayer = Bukkit.getOfflinePlayer(firstArgument)
                val uuid = selectedPlayer.uniqueId.toString()
                region.whitelisteds.remove(uuid)
                println(uuid)
                event.player.sendMessage(messagesSection.getString("whitelist-removed").translateColors())
            }
            else -> return
        }
        operationMap.remove(event.player)
        plugin.db.updateRegion(region)
    }

    @EventHandler
    fun onQuit(event: PlayerQuitEvent) {
        operationMap.remove(event.player)
        firstLocationMap.remove(event.player)
        selectedAreaMap.remove(event.player)
    }
}
