package com.github.liberatemetumortis.region

import com.github.liberatemetumortis.region.data.Area
import com.github.liberatemetumortis.region.data.Database
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import java.io.File

class Main : JavaPlugin() {
    companion object {
        val selectedAreaMap = mutableMapOf<Player, Area>()
        val firstLocationMap = mutableMapOf<Player, Location>()
    }
    lateinit var selectWand: SelectWand
    lateinit var db: Database
    override fun onEnable() {
        saveDefaultConfig()
        reloadConfig()
        selectWand = SelectWand(this.config)
        val dbFile = File(dataFolder, "db.sqlite")
        db = Database(dbFile.absolutePath, this)
        db.fetchRegions()
        EventListener(this)
        CommandHandler(this)
    }

    override fun onDisable() {
        db.closeConnection()
    }
}
