package com.acme.transport.linehaul.capacity.slot

import com.acme.transport.kit.text.truncate

/** How screens and exports show a fallback capacity slot. */
data class FallbackCapacitySlotView(
    var code: String,
    var label: String,
    var category: String,
    var reviewer: String,
    var checksumText: String,
    var weightText: String,
    var pallets: String,
    var timezoneText: String,
    var titleText: String,
    var statusLabel: String,
)

fun toFallbackCapacitySlotView(item: FallbackCapacitySlot): FallbackCapacitySlotView {
    return FallbackCapacitySlotView(
        code = truncate(item.code, 64),
        label = truncate(item.label, 40),
        category = item.category,
        reviewer = item.reviewer,
        checksumText = item.checksum,
        weightText = "%.2f".format(item.weight),
        pallets = item.pallets.toString(),
        timezoneText = item.timezone,
        titleText = truncate(item.title, 80),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toFallbackCapacitySlotViews(items: MutableList<FallbackCapacitySlot>): MutableList<FallbackCapacitySlotView> {
    val views = mutableListOf<FallbackCapacitySlotView>()
    for (item in items) {
        views.add(toFallbackCapacitySlotView(item))
    }
    return views
}

fun sumFallbackCapacitySlotView(view: FallbackCapacitySlotView): Double {
    val parsedWeight = view.weightText.toDouble()
    val parsedPallets = view.pallets.toInt()
    return parsedWeight + parsedPallets.toDouble()
}
