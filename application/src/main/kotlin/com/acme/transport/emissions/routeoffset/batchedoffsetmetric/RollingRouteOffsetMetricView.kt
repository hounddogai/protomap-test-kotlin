package com.acme.transport.emissions.routeoffset.batchedoffsetmetric

import com.acme.transport.kit.text.truncate

/** How screens and exports show a rolling route offset metric. */
data class RollingRouteOffsetMetricView(
    var code: String,
    var titleText: String,
    var ceilingText: String,
    var pallets: String,
    var fuelLitersText: String,
    var unit: String,
    var thresholdText: String,
    var capacity: String,
    var trailerIdText: String,
    var statusLabel: String,
)

fun toRollingRouteOffsetMetricView(item: RollingRouteOffsetMetric): RollingRouteOffsetMetricView {
    return RollingRouteOffsetMetricView(
        code = item.code,
        titleText = truncate(item.title, 120),
        ceilingText = "%.2f".format(item.ceiling),
        pallets = item.pallets.toString(),
        fuelLitersText = "%.2f".format(item.fuelLiters),
        unit = item.unit,
        thresholdText = "%.2f".format(item.threshold),
        capacity = item.capacity.toString(),
        trailerIdText = item.trailerId,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRollingRouteOffsetMetricViews(items: MutableList<RollingRouteOffsetMetric>): MutableList<RollingRouteOffsetMetricView> {
    val views = mutableListOf<RollingRouteOffsetMetricView>()
    for (item in items) {
        views.add(toRollingRouteOffsetMetricView(item))
    }
    return views
}

fun sumRollingRouteOffsetMetricView(view: RollingRouteOffsetMetricView): Double {
    val parsedCeiling = view.ceilingText.toDouble()
    val parsedPallets = view.pallets.toInt()
    val parsedFuelLiters = view.fuelLitersText.toDouble()
    return parsedCeiling + parsedPallets.toDouble() + parsedFuelLiters
}
