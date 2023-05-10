package com.github.liberatemetumortis.region.gui.regions

import com.github.liberatemetumortis.region.data.Region
import com.github.liberatemetumortis.region.gui.PageUtils
import com.github.liberatemetumortis.region.gui.PageUtils.Companion.FILLER_SLOTS
import com.github.liberatemetumortis.region.gui.PageUtils.Companion.REGION_PER_PAGE
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.Player
import kotlin.math.ceil
import kotlin.math.min

class RegionPageCreater(val player: Player, val configurationSection: ConfigurationSection) {
    val pages = mutableListOf<RegionPage>()
    private var currentIndex = 0
    init {
        val regionsOfThePlayer = Region.regions.filter { it.owner == player.uniqueId.toString() }
        val pagesCount = ceil(regionsOfThePlayer.size / REGION_PER_PAGE.toDouble()).toInt()
        val title = configurationSection.getString("title")
        val fillerItem = PageUtils.getItem("filler", configurationSection)
        val previousPage = PageUtils.getItem("previous-page", configurationSection)
        val nextPage = PageUtils.getItem("next-page", configurationSection)

        for (i in 0 until pagesCount) {
            val page = RegionPage(title!!, this, regionsOfThePlayer.subList(i, min(i + REGION_PER_PAGE, regionsOfThePlayer.size)))
            page.inventory.setItem(45, previousPage)
            page.inventory.setItem(53, nextPage)
            for (slot in FILLER_SLOTS) {
                page.inventory.setItem(slot, fillerItem)
            }
            pages.add(page)
        }
    }
    fun openGUI() {
        player.openInventory(pages[currentIndex].inventory)
    }
    fun nextPage() {
        if (currentIndex + 1 >= pages.size) return
        player.openInventory(pages[++currentIndex].inventory)
    }
    fun previousPage() {
        if (currentIndex - 1 < 0) return
        player.openInventory(pages[--currentIndex].inventory)
    }
}
