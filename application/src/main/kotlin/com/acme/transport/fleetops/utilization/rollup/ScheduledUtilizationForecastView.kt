package com.acme.transport.fleetops.utilization.rollup

import com.acme.transport.kit.text.truncate

/** How screens and exports show a scheduled utilization forecast. */
data class ScheduledUtilizationForecastView(
    var code: String,
    var name: String,
    var trailerIdText: String,
    var cost: String,
    var sequenceText: String,
    var thresholdText: String,
    var region: String,
    var statusLabel: String,
)

fun toScheduledUtilizationForecastView(item: ScheduledUtilizationForecast): ScheduledUtilizationForecastView {
    return ScheduledUtilizationForecastView(
        code = item.code,
        name = truncate(item.name, 40),
        trailerIdText = item.trailerId,
        cost = "%.2f".format(item.cost),
        sequenceText = item.sequence.toString(),
        thresholdText = "%.2f".format(item.threshold),
        region = truncate(item.region, 120),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toScheduledUtilizationForecastViews(items: MutableList<ScheduledUtilizationForecast>): MutableList<ScheduledUtilizationForecastView> {
    val views = mutableListOf<ScheduledUtilizationForecastView>()
    for (item in items) {
        views.add(toScheduledUtilizationForecastView(item))
    }
    return views
}

fun sumScheduledUtilizationForecastView(view: ScheduledUtilizationForecastView): Double {
    val parsedCost = view.cost.toDouble()
    val parsedSequence = view.sequenceText.toInt()
    val parsedThreshold = view.thresholdText.toDouble()
    return parsedCost + parsedSequence.toDouble() + parsedThreshold
}
