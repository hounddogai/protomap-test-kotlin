package com.acme.transport.emissions.fuelburn.burnnotice

import com.acme.transport.kit.text.truncate

/** How screens and exports show a fuel burn metric. */
data class FuelBurnMetricView(
    var code: String,
    var nameText: String,
    var ceiling: String,
    var lane: String,
    var threshold: String,
    var maximum: String,
    var limit: String,
    var timezoneText: String,
    var pinned: String,
    var minimumText: String,
    var statusLabel: String,
)

fun toFuelBurnMetricView(item: FuelBurnMetric): FuelBurnMetricView {
    return FuelBurnMetricView(
        code = item.code,
        nameText = truncate(item.name, 40),
        ceiling = "%.2f".format(item.ceiling),
        lane = truncate(item.lane, 80),
        threshold = "%.2f".format(item.threshold),
        maximum = "%.2f".format(item.maximum),
        limit = item.limit.toString(),
        timezoneText = item.timezone,
        pinned = "${item.pinned}",
        minimumText = "%.2f".format(item.minimum),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toFuelBurnMetricViews(items: MutableList<FuelBurnMetric>): MutableList<FuelBurnMetricView> {
    val views = mutableListOf<FuelBurnMetricView>()
    for (item in items) {
        views.add(toFuelBurnMetricView(item))
    }
    return views
}

fun sumFuelBurnMetricView(view: FuelBurnMetricView): Double {
    val parsedCeiling = view.ceiling.toDouble()
    val parsedThreshold = view.threshold.toDouble()
    val parsedMaximum = view.maximum.toDouble()
    return parsedCeiling + parsedThreshold + parsedMaximum
}
