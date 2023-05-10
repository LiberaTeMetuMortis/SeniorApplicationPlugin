package com.github.liberatemetumortis.region.data

import org.bukkit.Location

class Area(val worldName: String, x1: Double, z1: Double, x2: Double, z2: Double) {
    val minX = x1.coerceAtMost(x2)
    val minZ = z1.coerceAtMost(z2)
    val maxX = x1.coerceAtLeast(x2)
    val maxZ = z1.coerceAtLeast(z2)
    companion object {
        fun from(loc1: Location, loc2: Location): Area? {
            if (loc1.world.name != loc2.world.name) return null
            return Area(loc1.world.name, loc1.x, loc1.z, loc2.x, loc2.z)
        }
    }

    fun intersects(other: Area): Boolean {
        return worldName == other.worldName && (minX <= other.maxX && maxX >= other.minX) && (minZ <= other.maxZ && maxZ >= other.minZ)
    }

    operator fun contains(location: Location): Boolean {
        return location.world.name == worldName && location.x in (minX..maxX) && location.z in (minZ..maxZ)
    }
}
