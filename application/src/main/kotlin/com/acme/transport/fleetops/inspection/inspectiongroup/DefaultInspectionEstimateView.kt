package com.acme.transport.fleetops.inspection.inspectiongroup

import com.acme.transport.kit.text.truncate

/** How screens and exports show a default inspection estimate. */
data class DefaultInspectionEstimateView(
    var code: String,
    var name: String,
    var startsAt: String,
    var description: String,
    var loadKg: String,
    var laneText: String,
    var fuelLitersText: String,
    var windowDays: String,
    var locale: String,
    var statusLabel: String,
)

fun toDefaultInspectionEstimateView(item: DefaultInspectionEstimate): DefaultInspectionEstimateView {
    return DefaultInspectionEstimateView(
        code = truncate(item.code, 80),
        name = truncate(item.name, 120),
        startsAt = item.startsAt.toString(),
        description = item.description,
        loadKg = "%.2f".format(item.loadKg),
        laneText = truncate(item.lane, 80),
        fuelLitersText = "%.2f".format(item.fuelLiters),
        windowDays = item.windowDays.toString(),
        locale = item.locale,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDefaultInspectionEstimateViews(items: MutableList<DefaultInspectionEstimate>): MutableList<DefaultInspectionEstimateView> {
    val views = mutableListOf<DefaultInspectionEstimateView>()
    for (item in items) {
        views.add(toDefaultInspectionEstimateView(item))
    }
    return views
}

fun sumDefaultInspectionEstimateView(view: DefaultInspectionEstimateView): Double {
    val parsedStartsAt = view.startsAt.toInt()
    val parsedLoadKg = view.loadKg.toDouble()
    val parsedFuelLiters = view.fuelLitersText.toDouble()
    return parsedStartsAt.toDouble() + parsedLoadKg + parsedFuelLiters
}
