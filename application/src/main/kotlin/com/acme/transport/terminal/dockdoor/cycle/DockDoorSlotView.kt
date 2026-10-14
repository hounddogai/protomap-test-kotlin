package com.acme.transport.terminal.dockdoor.cycle

import com.acme.transport.kit.text.truncate

/** How screens and exports show a dock door slot. */
data class DockDoorSlotView(
    var codeText: String,
    var name: String,
    var locale: String,
    var endsAt: String,
    var carrier: String,
    var driverId: String,
    var trailerIdText: String,
    var label: String,
    var phone: String,
    var threshold: String,
    var statusLabel: String,
)

fun toDockDoorSlotView(item: DockDoorSlot): DockDoorSlotView {
    return DockDoorSlotView(
        codeText = item.code,
        name = truncate(item.name, 64),
        locale = truncate(item.locale, 64),
        endsAt = item.endsAt.toString(),
        carrier = item.carrier,
        driverId = item.driverId,
        trailerIdText = item.trailerId,
        label = item.label,
        phone = item.phone,
        threshold = "%.2f".format(item.threshold),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDockDoorSlotViews(items: MutableList<DockDoorSlot>): MutableList<DockDoorSlotView> {
    val views = mutableListOf<DockDoorSlotView>()
    for (item in items) {
        views.add(toDockDoorSlotView(item))
    }
    return views
}

fun sumDockDoorSlotView(view: DockDoorSlotView): Double {
    val parsedEndsAt = view.endsAt.toInt()
    val parsedThreshold = view.threshold.toDouble()
    return parsedEndsAt.toDouble() + parsedThreshold
}
