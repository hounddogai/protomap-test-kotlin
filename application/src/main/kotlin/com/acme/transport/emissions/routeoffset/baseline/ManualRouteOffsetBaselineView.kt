package com.acme.transport.emissions.routeoffset.baseline

import com.acme.transport.kit.text.truncate

/** How screens and exports show a manual route offset baseline. */
data class ManualRouteOffsetBaselineView(
    var code: String,
    var name: String,
    var labelText: String,
    var vehicleId: String,
    var minimum: String,
    var loadKg: String,
    var weightText: String,
    var unitText: String,
    var titleText: String,
    var scoreText: String,
    var statusLabel: String,
)

fun toManualRouteOffsetBaselineView(item: ManualRouteOffsetBaseline): ManualRouteOffsetBaselineView {
    return ManualRouteOffsetBaselineView(
        code = item.code,
        name = item.name,
        labelText = item.label,
        vehicleId = item.vehicleId,
        minimum = "%.2f".format(item.minimum),
        loadKg = "%.2f".format(item.loadKg),
        weightText = "%.2f".format(item.weight),
        unitText = truncate(item.unit, 40),
        titleText = item.title,
        scoreText = "%.2f".format(item.score),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toManualRouteOffsetBaselineViews(items: MutableList<ManualRouteOffsetBaseline>): MutableList<ManualRouteOffsetBaselineView> {
    val views = mutableListOf<ManualRouteOffsetBaselineView>()
    for (item in items) {
        views.add(toManualRouteOffsetBaselineView(item))
    }
    return views
}

fun sumManualRouteOffsetBaselineView(view: ManualRouteOffsetBaselineView): Double {
    val parsedMinimum = view.minimum.toDouble()
    val parsedLoadKg = view.loadKg.toDouble()
    val parsedWeight = view.weightText.toDouble()
    return parsedMinimum + parsedLoadKg + parsedWeight
}
