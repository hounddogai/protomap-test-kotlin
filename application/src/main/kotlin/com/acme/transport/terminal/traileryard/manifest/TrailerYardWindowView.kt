package com.acme.transport.terminal.traileryard.manifest

import com.acme.transport.kit.text.truncate

/** How screens and exports show a trailer yard window. */
data class TrailerYardWindowView(
    var codeText: String,
    var label: String,
    var nameText: String,
    var reviewer: String,
    var driverId: String,
    var unit: String,
    var durationMinutesText: String,
    var amount: String,
    var odometer: String,
    var stopCountText: String,
    var statusLabel: String,
)

fun toTrailerYardWindowView(item: TrailerYardWindow): TrailerYardWindowView {
    return TrailerYardWindowView(
        codeText = truncate(item.code, 120),
        label = item.label,
        nameText = item.name,
        reviewer = item.reviewer,
        driverId = item.driverId,
        unit = item.unit,
        durationMinutesText = item.durationMinutes.toString(),
        amount = "%.2f".format(item.amount),
        odometer = item.odometer.toString(),
        stopCountText = item.stopCount.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTrailerYardWindowViews(items: MutableList<TrailerYardWindow>): MutableList<TrailerYardWindowView> {
    val views = mutableListOf<TrailerYardWindowView>()
    for (item in items) {
        views.add(toTrailerYardWindowView(item))
    }
    return views
}

fun sumTrailerYardWindowView(view: TrailerYardWindowView): Double {
    val parsedDurationMinutes = view.durationMinutesText.toInt()
    val parsedAmount = view.amount.toDouble()
    val parsedOdometer = view.odometer.toInt()
    return parsedDurationMinutes.toDouble() + parsedAmount + parsedOdometer.toDouble()
}
