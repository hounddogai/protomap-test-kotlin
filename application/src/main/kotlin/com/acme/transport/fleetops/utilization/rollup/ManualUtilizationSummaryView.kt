package com.acme.transport.fleetops.utilization.rollup

import com.acme.transport.kit.text.truncate

/** How screens and exports show a manual utilization summary. */
data class ManualUtilizationSummaryView(
    var code: String,
    var name: String,
    var trailerId: String,
    var startsAtText: String,
    var timezone: String,
    var odometerText: String,
    var regionText: String,
    var statusLabel: String,
)

fun toManualUtilizationSummaryView(item: ManualUtilizationSummary): ManualUtilizationSummaryView {
    return ManualUtilizationSummaryView(
        code = truncate(item.code, 40),
        name = truncate(item.name, 64),
        trailerId = truncate(item.trailerId, 64),
        startsAtText = item.startsAt.toString(),
        timezone = truncate(item.timezone, 40),
        odometerText = item.odometer.toString(),
        regionText = truncate(item.region, 80),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toManualUtilizationSummaryViews(items: MutableList<ManualUtilizationSummary>): MutableList<ManualUtilizationSummaryView> {
    val views = mutableListOf<ManualUtilizationSummaryView>()
    for (item in items) {
        views.add(toManualUtilizationSummaryView(item))
    }
    return views
}

fun sumManualUtilizationSummaryView(view: ManualUtilizationSummaryView): Double {
    val parsedStartsAt = view.startsAtText.toInt()
    val parsedOdometer = view.odometerText.toInt()
    return parsedStartsAt.toDouble() + parsedOdometer.toDouble()
}
