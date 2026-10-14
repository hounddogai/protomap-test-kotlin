package com.acme.transport.emissions.fuelburn.burnwindow

import com.acme.transport.kit.text.truncate

/** How screens and exports show a pending fuel burn window. */
data class PendingFuelBurnWindowView(
    var code: String,
    var title: String,
    var shiftCodeText: String,
    var unitText: String,
    var phone: String,
    var ratio: String,
    var graceDaysText: String,
    var trailerId: String,
    var laneText: String,
    var statusLabel: String,
)

fun toPendingFuelBurnWindowView(item: PendingFuelBurnWindow): PendingFuelBurnWindowView {
    return PendingFuelBurnWindowView(
        code = item.code,
        title = item.title,
        shiftCodeText = item.shiftCode,
        unitText = truncate(item.unit, 64),
        phone = item.phone,
        ratio = "%.2f".format(item.ratio),
        graceDaysText = item.graceDays.toString(),
        trailerId = truncate(item.trailerId, 64),
        laneText = truncate(item.lane, 40),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPendingFuelBurnWindowViews(items: MutableList<PendingFuelBurnWindow>): MutableList<PendingFuelBurnWindowView> {
    val views = mutableListOf<PendingFuelBurnWindowView>()
    for (item in items) {
        views.add(toPendingFuelBurnWindowView(item))
    }
    return views
}

fun sumPendingFuelBurnWindowView(view: PendingFuelBurnWindowView): Double {
    val parsedRatio = view.ratio.toDouble()
    val parsedGraceDays = view.graceDaysText.toInt()
    return parsedRatio + parsedGraceDays.toDouble()
}
