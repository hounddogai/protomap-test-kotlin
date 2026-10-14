package com.acme.transport.linehaul.capacity.rollup

import com.acme.transport.kit.text.truncate

/** How screens and exports show a capacity slot. */
data class CapacitySlotView(
    var code: String,
    var label: String,
    var endsAt: String,
    var pinned: String,
    var rate: String,
    var limit: String,
    var dockText: String,
    var statusLabel: String,
)

fun toCapacitySlotView(item: CapacitySlot): CapacitySlotView {
    return CapacitySlotView(
        code = truncate(item.code, 40),
        label = item.label,
        endsAt = item.endsAt.toString(),
        pinned = "${item.pinned}",
        rate = "%.2f".format(item.rate),
        limit = item.limit.toString(),
        dockText = item.dock,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCapacitySlotViews(items: MutableList<CapacitySlot>): MutableList<CapacitySlotView> {
    val views = mutableListOf<CapacitySlotView>()
    for (item in items) {
        views.add(toCapacitySlotView(item))
    }
    return views
}

fun sumCapacitySlotView(view: CapacitySlotView): Double {
    val parsedEndsAt = view.endsAt.toInt()
    val parsedRate = view.rate.toDouble()
    val parsedLimit = view.limit.toInt()
    return parsedEndsAt.toDouble() + parsedRate + parsedLimit.toDouble()
}
