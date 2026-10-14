package com.acme.transport.terminal.dockdoor.slot

import com.acme.transport.kit.text.truncate

/** How screens and exports show a fallback dock door slot. */
data class FallbackDockDoorSlotView(
    var codeText: String,
    var label: String,
    var categoryText: String,
    var localeText: String,
    var version: String,
    var statusLabel: String,
)

fun toFallbackDockDoorSlotView(item: FallbackDockDoorSlot): FallbackDockDoorSlotView {
    return FallbackDockDoorSlotView(
        codeText = item.code,
        label = item.label,
        categoryText = item.category,
        localeText = truncate(item.locale, 40),
        version = item.version.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toFallbackDockDoorSlotViews(items: MutableList<FallbackDockDoorSlot>): MutableList<FallbackDockDoorSlotView> {
    val views = mutableListOf<FallbackDockDoorSlotView>()
    for (item in items) {
        views.add(toFallbackDockDoorSlotView(item))
    }
    return views
}

fun sumFallbackDockDoorSlotView(view: FallbackDockDoorSlotView): Double {
    val parsedVersion = view.version.toInt()
    return parsedVersion.toDouble()
}
