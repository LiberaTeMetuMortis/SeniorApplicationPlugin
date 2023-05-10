package com.github.liberatemetumortis.region

import com.github.liberatemetumortis.region.Main.Companion.selectedAreaMap
import com.github.liberatemetumortis.region.Utils.Companion.translateColors
import com.github.liberatemetumortis.region.data.Region
import com.github.liberatemetumortis.region.gui.regions.RegionPageCreater
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.util.*

/*
/region - Opens the regions menu
/region create <name> - Creates a region at the selected location
/region wand - Gives the user a stick with a custom name to select locations to create a region
/region add <name> <username> - Whitelist a user to a region
/region remove <name> <username> - Removes a user from the region whitelist
/region whitelist <name> - Lists the users in the region whitelist
/region <name> - Opens the region menu

 */
class CommandHandler(val plugin: Main) : CommandExecutor {
    val config = plugin.config
    init {
        plugin.getCommand("region")?.setExecutor(this)
    }
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (args.isEmpty()) { // Menu
            if (sender !is Player) { // Check for player
                sender.sendMessage(config.getString("messages.not-a-player").translateColors())
                return true
            }
            if (Region.regions.none { it.owner == sender.uniqueId.toString() }) {
                sender.sendMessage(config.getString("messages.no-regions").translateColors())
                return true
            }
            val regionPageCreater = RegionPageCreater(sender, plugin.config.getConfigurationSection("gui.regions")!!)
            regionPageCreater.openGUI()
            return true
        } else {
            val operation = args[0].lowercase()
            if (!sender.hasPermission("region.$operation")) {
                sender.sendMessage(config.getString("messages.no-permission").translateColors())
                return true
            }
            when (operation) {
                "create" -> {
                    if (sender !is Player) { // Check for player
                        sender.sendMessage(config.getString("messages.not-a-player").translateColors())
                        return true
                    }
                    if (args.size == 2) { // Check for name
                        val name = args[1]

                        // Check for is area selected
                        selectedAreaMap[sender] ?: run {
                            sender.sendMessage(config.getString("messages.no-area-selected").translateColors())
                            return true
                        }

                        // Check for is name unique
                        if (Region.regions.any { (it.owner == sender.uniqueId.toString()) && (it.name == name) }) {
                            sender.sendMessage(config.getString("messages.region-exists").translateColors())
                            return true
                        }

                        // Check for intersects
                        val area = selectedAreaMap[sender]!!
                        if (Region.regions.any { it.area.intersects(area) }) {
                            sender.sendMessage(config.getString("messages.area-intersects").translateColors())
                            return true
                        }

                        // Create region
                        Region(name, sender.uniqueId.toString(), area).also(plugin.db::insertRegion)
                        sender.sendMessage(config.getString("messages.region-created").translateColors())
                    } else {
                        sender.sendMessage(config.getString("usages.create").translateColors())
                    }
                }

                "wand" -> {
                    if (sender !is Player) { // Check for player
                        sender.sendMessage(config.getString("messages.not-a-player").translateColors())
                        return true
                    }
                    plugin.selectWand.giveWand(sender)
                    sender.sendMessage(config.getString("messages.gave-wand").translateColors())
                }

                "add" -> {
                    if (sender !is Player) { // Check for player
                        sender.sendMessage(config.getString("messages.not-a-player").translateColors())
                        return true
                    }
                    if (args.size == 3) {
                        val name = args[1]
                        val username = args[2]
                        val player = Bukkit.getPlayer(username)
                        if (player == null) {
                            sender.sendMessage(config.getString("messages.player-not-found").translateColors())
                            return true
                        }
                        val region = Region.regions.find { it.name == name && it.owner == sender.uniqueId.toString() } ?: run {
                            sender.sendMessage(config.getString("messages.region-not-found").translateColors())
                            return true
                        }
                        if (region.whitelisteds.contains(player.uniqueId.toString())) {
                            sender.sendMessage(config.getString("messages.already-whitelisted").translateColors())
                            return true
                        }
                        region.whitelisteds.add(player.uniqueId.toString())
                        plugin.db.updateRegion(region)
                        sender.sendMessage(config.getString("messages.whitelisted").translateColors())
                    } else {
                        sender.sendMessage(config.getString("usages.add").translateColors())
                    }
                }

                "remove" -> {
                    if (sender !is Player) { // Check for player
                        sender.sendMessage(config.getString("messages.not-a-player").translateColors())
                        return true
                    }
                    if (args.size == 3) {
                        val name = args[1]
                        val username = args[2]
                        val player = Bukkit.getOfflinePlayer(username)
                        val region = Region.regions.find { it.name == name && it.owner == sender.uniqueId.toString() } ?: run {
                            sender.sendMessage(config.getString("messages.region-not-found").translateColors())
                            return true
                        }
                        if (!region.whitelisteds.contains(player.uniqueId.toString())) {
                            sender.sendMessage(config.getString("messages.not-whitelisted").translateColors())
                            return true
                        }
                        region.whitelisteds.remove(player.uniqueId.toString())
                        plugin.db.updateRegion(region)
                        sender.sendMessage(config.getString("messages.unwhitelisted").translateColors())
                    } else {
                        sender.sendMessage(config.getString("usages.remove").translateColors())
                    }
                }

                "whitelist" -> {
                    if (sender !is Player) {
                        sender.sendMessage(config.getString("messages.not-a-player").translateColors())
                        return true
                    }
                    if (args.size == 2) {
                        val name = args[1]
                        val region = Region.regions.find { it.name == name && it.owner == sender.uniqueId.toString() } ?: run {
                            sender.sendMessage(config.getString("messages.region-not-found").translateColors())
                            return true
                        }
                        sender.sendMessage(config.getString("messages.whitelist.title").translateColors())
                        for (playerUUID in region.whitelisteds) {
                            val player = Bukkit.getOfflinePlayer(UUID.fromString(playerUUID))
                            sender.sendMessage(config.getString("messages.whitelist.entry")!!.replace("%player%", player.name!!).translateColors())
                        }
                    } else {
                        sender.sendMessage(config.getString("usages.whitelist").translateColors())
                    }
                }
            }
        }
        return true
    }
}
