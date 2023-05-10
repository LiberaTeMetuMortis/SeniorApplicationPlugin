package com.github.liberatemetumortis.region

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer

class Utils {
    companion object {
        fun String?.translateColors(): TextComponent {
            if (this == null) return Component.empty()
            return LegacyComponentSerializer.legacySection().deserialize(this.replace('&', '§'))
        }
        fun String?.stripColors(): String {
            if (this == null) return ""
            return PlainTextComponentSerializer.plainText().serialize(LegacyComponentSerializer.legacySection().deserialize(this))
        }
    }
}
