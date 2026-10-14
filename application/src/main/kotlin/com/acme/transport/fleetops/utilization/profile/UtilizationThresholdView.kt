package com.acme.transport.fleetops.utilization.profile

import com.acme.transport.kit.text.truncate

/** How screens and exports show a utilization threshold. */
data class UtilizationThresholdView(
    var code: String,
    var name: String,
    var distanceKm: String,
    var thresholdText: String,
    var localeText: String,
    var amount: String,
    var discount: String,
    var odometer: String,
    var category: String,
    var startsAtText: String,
    var statusLabel: String,
)

fun toUtilizationThresholdView(item: UtilizationThreshold): UtilizationThresholdView {
    return UtilizationThresholdView(
        code = item.code,
        name = truncate(item.name, 120),
        distanceKm = "%.2f".format(item.distanceKm),
        thresholdText = "%.2f".format(item.threshold),
        localeText = item.locale,
        amount = "%.2f".format(item.amount),
        discount = "%.2f".format(item.discount),
        odometer = item.odometer.toString(),
        category = item.category,
        startsAtText = item.startsAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toUtilizationThresholdViews(items: MutableList<UtilizationThreshold>): MutableList<UtilizationThresholdView> {
    val views = mutableListOf<UtilizationThresholdView>()
    for (item in items) {
        views.add(toUtilizationThresholdView(item))
    }
    return views
}

fun sumUtilizationThresholdView(view: UtilizationThresholdView): Double {
    val parsedDistanceKm = view.distanceKm.toDouble()
    val parsedThreshold = view.thresholdText.toDouble()
    val parsedAmount = view.amount.toDouble()
    return parsedDistanceKm + parsedThreshold + parsedAmount
}
