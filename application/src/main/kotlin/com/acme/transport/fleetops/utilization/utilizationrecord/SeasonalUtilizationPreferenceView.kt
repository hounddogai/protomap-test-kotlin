package com.acme.transport.fleetops.utilization.utilizationrecord

import com.acme.transport.kit.text.truncate

/** How screens and exports show a seasonal utilization preference. */
data class SeasonalUtilizationPreferenceView(
    var code: String,
    var name: String,
    var distanceKmText: String,
    var externalRef: String,
    var minimum: String,
    var limitText: String,
    var odometer: String,
    var driverId: String,
    var statusLabel: String,
)

fun toSeasonalUtilizationPreferenceView(item: SeasonalUtilizationPreference): SeasonalUtilizationPreferenceView {
    return SeasonalUtilizationPreferenceView(
        code = truncate(item.code, 40),
        name = truncate(item.name, 40),
        distanceKmText = "%.2f".format(item.distanceKm),
        externalRef = item.externalRef,
        minimum = "%.2f".format(item.minimum),
        limitText = item.limit.toString(),
        odometer = item.odometer.toString(),
        driverId = truncate(item.driverId, 64),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toSeasonalUtilizationPreferenceViews(items: MutableList<SeasonalUtilizationPreference>): MutableList<SeasonalUtilizationPreferenceView> {
    val views = mutableListOf<SeasonalUtilizationPreferenceView>()
    for (item in items) {
        views.add(toSeasonalUtilizationPreferenceView(item))
    }
    return views
}

fun sumSeasonalUtilizationPreferenceView(view: SeasonalUtilizationPreferenceView): Double {
    val parsedDistanceKm = view.distanceKmText.toDouble()
    val parsedMinimum = view.minimum.toDouble()
    val parsedLimit = view.limitText.toInt()
    return parsedDistanceKm + parsedMinimum + parsedLimit.toDouble()
}
