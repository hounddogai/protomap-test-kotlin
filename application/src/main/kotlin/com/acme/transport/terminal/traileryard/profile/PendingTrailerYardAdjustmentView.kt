package com.acme.transport.terminal.traileryard.profile

import com.acme.transport.kit.text.truncate

/** How screens and exports show a pending trailer yard adjustment. */
data class PendingTrailerYardAdjustmentView(
    var code: String,
    var labelText: String,
    var odometer: String,
    var reviewer: String,
    var capacity: String,
    var maximumText: String,
    var cost: String,
    var fuelLiters: String,
    var currencyText: String,
    var active: String,
    var statusLabel: String,
)

fun toPendingTrailerYardAdjustmentView(item: PendingTrailerYardAdjustment): PendingTrailerYardAdjustmentView {
    return PendingTrailerYardAdjustmentView(
        code = truncate(item.code, 80),
        labelText = truncate(item.label, 120),
        odometer = item.odometer.toString(),
        reviewer = item.reviewer,
        capacity = item.capacity.toString(),
        maximumText = "%.2f".format(item.maximum),
        cost = "%.2f".format(item.cost),
        fuelLiters = "%.2f".format(item.fuelLiters),
        currencyText = item.currency,
        active = "${item.active}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPendingTrailerYardAdjustmentViews(items: MutableList<PendingTrailerYardAdjustment>): MutableList<PendingTrailerYardAdjustmentView> {
    val views = mutableListOf<PendingTrailerYardAdjustmentView>()
    for (item in items) {
        views.add(toPendingTrailerYardAdjustmentView(item))
    }
    return views
}

fun sumPendingTrailerYardAdjustmentView(view: PendingTrailerYardAdjustmentView): Double {
    val parsedOdometer = view.odometer.toInt()
    val parsedCapacity = view.capacity.toInt()
    val parsedMaximum = view.maximumText.toDouble()
    return parsedOdometer.toDouble() + parsedCapacity.toDouble() + parsedMaximum
}
