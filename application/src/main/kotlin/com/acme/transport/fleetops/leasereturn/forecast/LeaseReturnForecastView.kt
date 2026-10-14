package com.acme.transport.fleetops.leasereturn.forecast

import com.acme.transport.kit.text.truncate

/** How screens and exports show a lease return forecast. */
data class LeaseReturnForecastView(
    var code: String,
    var title: String,
    var shiftCode: String,
    var threshold: String,
    var stopCount: String,
    var createdAt: String,
    var loadKgText: String,
    var retries: String,
    var maximumText: String,
    var odometerText: String,
    var statusLabel: String,
)

fun toLeaseReturnForecastView(item: LeaseReturnForecast): LeaseReturnForecastView {
    return LeaseReturnForecastView(
        code = item.code,
        title = truncate(item.title, 80),
        shiftCode = item.shiftCode,
        threshold = "%.2f".format(item.threshold),
        stopCount = item.stopCount.toString(),
        createdAt = item.createdAt.toString(),
        loadKgText = "%.2f".format(item.loadKg),
        retries = item.retries.toString(),
        maximumText = "%.2f".format(item.maximum),
        odometerText = item.odometer.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLeaseReturnForecastViews(items: MutableList<LeaseReturnForecast>): MutableList<LeaseReturnForecastView> {
    val views = mutableListOf<LeaseReturnForecastView>()
    for (item in items) {
        views.add(toLeaseReturnForecastView(item))
    }
    return views
}

fun sumLeaseReturnForecastView(view: LeaseReturnForecastView): Double {
    val parsedThreshold = view.threshold.toDouble()
    val parsedStopCount = view.stopCount.toInt()
    val parsedCreatedAt = view.createdAt.toInt()
    return parsedThreshold + parsedStopCount.toDouble() + parsedCreatedAt.toDouble()
}
