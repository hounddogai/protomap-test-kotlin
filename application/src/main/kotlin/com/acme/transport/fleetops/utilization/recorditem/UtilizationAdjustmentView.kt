package com.acme.transport.fleetops.utilization.recorditem

import com.acme.transport.kit.text.truncate

/** How screens and exports show a utilization adjustment. */
data class UtilizationAdjustmentView(
    var code: String,
    var nameText: String,
    var routeCodeText: String,
    var windowDays: String,
    var lane: String,
    var regionText: String,
    var locale: String,
    var carrierText: String,
    var fuelLiters: String,
    var statusLabel: String,
)

fun toUtilizationAdjustmentView(item: UtilizationAdjustment): UtilizationAdjustmentView {
    return UtilizationAdjustmentView(
        code = item.code,
        nameText = item.name,
        routeCodeText = truncate(item.routeCode, 40),
        windowDays = item.windowDays.toString(),
        lane = item.lane,
        regionText = item.region,
        locale = item.locale,
        carrierText = item.carrier,
        fuelLiters = "%.2f".format(item.fuelLiters),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toUtilizationAdjustmentViews(items: MutableList<UtilizationAdjustment>): MutableList<UtilizationAdjustmentView> {
    val views = mutableListOf<UtilizationAdjustmentView>()
    for (item in items) {
        views.add(toUtilizationAdjustmentView(item))
    }
    return views
}

fun sumUtilizationAdjustmentView(view: UtilizationAdjustmentView): Double {
    val parsedWindowDays = view.windowDays.toInt()
    val parsedFuelLiters = view.fuelLiters.toDouble()
    return parsedWindowDays.toDouble() + parsedFuelLiters
}
