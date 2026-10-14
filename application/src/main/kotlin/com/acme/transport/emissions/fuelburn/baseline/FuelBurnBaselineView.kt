package com.acme.transport.emissions.fuelburn.baseline

import com.acme.transport.kit.text.truncate

/** How screens and exports show a fuel burn baseline. */
data class FuelBurnBaselineView(
    var code: String,
    var name: String,
    var windowDays: String,
    var floorValue: String,
    var approved: String,
    var ownerId: String,
    var rate: String,
    var lane: String,
    var phone: String,
    var statusLabel: String,
)

fun toFuelBurnBaselineView(item: FuelBurnBaseline): FuelBurnBaselineView {
    return FuelBurnBaselineView(
        code = truncate(item.code, 80),
        name = item.name,
        windowDays = item.windowDays.toString(),
        floorValue = "%.2f".format(item.floorValue),
        approved = "${item.approved}",
        ownerId = item.ownerId,
        rate = "%.2f".format(item.rate),
        lane = item.lane,
        phone = item.phone,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toFuelBurnBaselineViews(items: MutableList<FuelBurnBaseline>): MutableList<FuelBurnBaselineView> {
    val views = mutableListOf<FuelBurnBaselineView>()
    for (item in items) {
        views.add(toFuelBurnBaselineView(item))
    }
    return views
}

fun sumFuelBurnBaselineView(view: FuelBurnBaselineView): Double {
    val parsedWindowDays = view.windowDays.toInt()
    val parsedFloorValue = view.floorValue.toDouble()
    val parsedRate = view.rate.toDouble()
    return parsedWindowDays.toDouble() + parsedFloorValue + parsedRate
}
