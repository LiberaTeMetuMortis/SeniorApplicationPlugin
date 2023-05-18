package com.github.liberatemetumortis.region.data

import com.github.liberatemetumortis.region.Main
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.scheduler.BukkitRunnable
import java.sql.DriverManager

class Database(val path: String, val plugin: Main) {
    private val connection by lazy {
        val databaseConfig: ConfigurationSection = plugin.getConfig().getConfigurationSection("database")!!
        val host = databaseConfig.getString("host")
        val port = databaseConfig.getString("port")
        val name = databaseConfig.getString("name")
        val username = databaseConfig.getString("username")
        val password = databaseConfig.getString("password")
        DriverManager.getConnection("jdbc:mysql://$host:$port/$name", username, password)
    }
    init {
        object : BukkitRunnable() {
            override fun run() {
                connection.createStatement().use { statement ->
                    statement.executeUpdate("CREATE TABLE IF NOT EXISTS regions (id INT NOT NULL AUTO_INCREMENT PRIMARY KEY, name VARCHAR(255), owner VARCHAR(36), whitelisteds TEXT, world VARCHAR(255), minX DOUBLE, minZ DOUBLE, maxX DOUBLE, maxZ DOUBLE)")
                }
            }
        }.runTaskAsynchronously(plugin)
    }

    fun insertRegion(region: Region) {
        object : BukkitRunnable() {
            override fun run() {
                connection.prepareStatement("INSERT INTO regions (name, owner, whitelisteds, world, minX, minZ, maxX, maxZ) VALUES (?, ?, ?, ?, ?, ?, ?, ?)").use { statement ->
                    statement.setString(1, region.name)
                    statement.setString(2, region.owner)
                    statement.setString(3, region.whitelisteds.joinToString(","))
                    statement.setString(4, region.area.worldName)
                    statement.setDouble(5, region.area.minX)
                    statement.setDouble(6, region.area.minZ)
                    statement.setDouble(7, region.area.maxX)
                    statement.setDouble(8, region.area.maxZ)
                    statement.executeUpdate()
                }
                connection.createStatement().use { statement ->
                    val result = statement.executeQuery("SELECT COUNT(*) as count from regions")
                    if (result.next()) {
                        region.id = result.getInt("count") + 1
                    }
                }
            }
        }.runTaskAsynchronously(plugin)
    }

    fun updateRegion(region: Region) {
        object : BukkitRunnable() {
            override fun run() {
                connection.prepareStatement("UPDATE regions SET name = ?, owner = ?, whitelisteds = ?, world = ?, minX = ?, minZ = ?, maxX = ?, maxZ = ? WHERE id = ?").use { statement ->
                    statement.setString(1, region.name)
                    statement.setString(2, region.owner)
                    statement.setString(3, region.whitelisteds.joinToString(","))
                    statement.setString(4, region.area.worldName)
                    statement.setDouble(5, region.area.minX)
                    statement.setDouble(6, region.area.minZ)
                    statement.setDouble(7, region.area.maxX)
                    statement.setDouble(8, region.area.maxZ)
                    statement.setInt(9, region.id)
                    statement.executeUpdate()
                }
            }
        }.runTaskAsynchronously(plugin)
    }

    fun fetchRegions() {
        object : BukkitRunnable() {
            override fun run() {
                connection.prepareStatement("SELECT * FROM regions").use { statement ->
                    statement.executeQuery().use { result ->
                        while (result.next()) {
                            val id = result.getInt("id")
                            val name = result.getString("name")
                            val owner = result.getString("owner")
                            val whitelisteds = result.getString("whitelisteds").split(",")
                            val world = result.getString("world")
                            val minX = result.getDouble("minX")
                            val minZ = result.getDouble("minZ")
                            val maxX = result.getDouble("maxX")
                            val maxZ = result.getDouble("maxZ")
                            Region(name, owner, Area(world, minX, minZ, maxX, maxZ)).apply {
                                this.whitelisteds.addAll(whitelisteds)
                                this.id = id
                            }
                        }
                    }
                }
            }
        }.runTaskAsynchronously(plugin)
    }

    fun closeConnection() {
        connection.close()
    }
}
