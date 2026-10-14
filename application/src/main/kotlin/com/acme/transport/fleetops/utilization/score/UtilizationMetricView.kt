package com.acme.transport.fleetops.utilization.score

import com.acme.transport.kit.text.truncate

/** How screens and exports show a utilization metric. */
data class UtilizationMetricView(
    var code: String,
    var name: String,
    var checksum: String,
    var stopCount: String,
    var odometer: String,
    var windowDaysText: String,
    var countryText: String,
    var budget: String,
    var title: String,
    var durationMinutes: String,
    var descriptionText: String,
    var statusLabel: String,
)

fun toUtilizationMetricView(item: UtilizationMetric): UtilizationMetricView {
    return UtilizationMetricView(
        code = item.code,
        name = item.name,
        checksum = truncate(item.checksum, 40),
        stopCount = item.stopCount.toString(),
        odometer = item.odometer.toString(),
        windowDaysText = item.windowDays.toString(),
        countryText = truncate(item.country, 40),
        budget = "%.2f".format(item.budget),
        title = truncate(item.title, 80),
        durationMinutes = item.durationMinutes.toString(),
        descriptionText = item.description,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toUtilizationMetricViews(items: MutableList<UtilizationMetric>): MutableList<UtilizationMetricView> {
    val views = mutableListOf<UtilizationMetricView>()
    for (item in items) {
        views.add(toUtilizationMetricView(item))
    }
    return views
}

fun sumUtilizationMetricView(view: UtilizationMetricView): Double {
    val parsedStopCount = view.stopCount.toInt()
    val parsedOdometer = view.odometer.toInt()
    val parsedWindowDays = view.windowDaysText.toInt()
    return parsedStopCount.toDouble() + parsedOdometer.toDouble() + parsedWindowDays.toDouble()
}
