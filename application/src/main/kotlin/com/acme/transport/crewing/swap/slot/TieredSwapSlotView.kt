package com.acme.transport.crewing.swap.slot

import com.acme.transport.kit.text.truncate

/** How screens and exports show a tiered swap slot. */
data class TieredSwapSlotView(
    var code: String,
    var labelText: String,
    var durationMinutes: String,
    var descriptionText: String,
    var capacity: String,
    var statusLabel: String,
)

fun toTieredSwapSlotView(item: TieredSwapSlot): TieredSwapSlotView {
    return TieredSwapSlotView(
        code = item.code,
        labelText = item.label,
        durationMinutes = item.durationMinutes.toString(),
        descriptionText = truncate(item.description, 40),
        capacity = item.capacity.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTieredSwapSlotViews(items: MutableList<TieredSwapSlot>): MutableList<TieredSwapSlotView> {
    val views = mutableListOf<TieredSwapSlotView>()
    for (item in items) {
        views.add(toTieredSwapSlotView(item))
    }
    return views
}

fun sumTieredSwapSlotView(view: TieredSwapSlotView): Double {
    val parsedDurationMinutes = view.durationMinutes.toInt()
    val parsedCapacity = view.capacity.toInt()
    return parsedDurationMinutes.toDouble() + parsedCapacity.toDouble()
}
