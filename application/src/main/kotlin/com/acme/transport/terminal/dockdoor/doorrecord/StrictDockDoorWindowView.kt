package com.acme.transport.terminal.dockdoor.doorrecord

import com.acme.transport.kit.text.truncate

/** How screens and exports show a strict dock door window. */
data class StrictDockDoorWindowView(
    var code: String,
    var name: String,
    var pinned: String,
    var fuelLitersText: String,
    var carrier: String,
    var summaryText: String,
    var statusLabel: String,
)

fun toStrictDockDoorWindowView(item: StrictDockDoorWindow): StrictDockDoorWindowView {
    return StrictDockDoorWindowView(
        code = item.code,
        name = truncate(item.name, 64),
        pinned = "${item.pinned}",
        fuelLitersText = "%.2f".format(item.fuelLiters),
        carrier = item.carrier,
        summaryText = item.summary,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toStrictDockDoorWindowViews(items: MutableList<StrictDockDoorWindow>): MutableList<StrictDockDoorWindowView> {
    val views = mutableListOf<StrictDockDoorWindowView>()
    for (item in items) {
        views.add(toStrictDockDoorWindowView(item))
    }
    return views
}

fun sumStrictDockDoorWindowView(view: StrictDockDoorWindowView): Double {
    val parsedFuelLiters = view.fuelLitersText.toDouble()
    return parsedFuelLiters
}
