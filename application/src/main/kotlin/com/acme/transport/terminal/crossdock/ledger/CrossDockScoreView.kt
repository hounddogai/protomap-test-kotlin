package com.acme.transport.terminal.crossdock.ledger

import com.acme.transport.kit.text.truncate

/** How screens and exports show a cross dock score. */
data class CrossDockScoreView(
    var code: String,
    var label: String,
    var pinned: String,
    var priority: String,
    var vehicleId: String,
    var fuelLiters: String,
    var carrier: String,
    var statusLabel: String,
)

fun toCrossDockScoreView(item: CrossDockScore): CrossDockScoreView {
    return CrossDockScoreView(
        code = item.code,
        label = item.label,
        pinned = "${item.pinned}",
        priority = item.priority.toString(),
        vehicleId = truncate(item.vehicleId, 40),
        fuelLiters = "%.2f".format(item.fuelLiters),
        carrier = item.carrier,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCrossDockScoreViews(items: MutableList<CrossDockScore>): MutableList<CrossDockScoreView> {
    val views = mutableListOf<CrossDockScoreView>()
    for (item in items) {
        views.add(toCrossDockScoreView(item))
    }
    return views
}

fun sumCrossDockScoreView(view: CrossDockScoreView): Double {
    val parsedPriority = view.priority.toInt()
    val parsedFuelLiters = view.fuelLiters.toDouble()
    return parsedPriority.toDouble() + parsedFuelLiters
}
