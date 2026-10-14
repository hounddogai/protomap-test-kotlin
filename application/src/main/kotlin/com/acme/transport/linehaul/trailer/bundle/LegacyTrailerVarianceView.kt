package com.acme.transport.linehaul.trailer.bundle

import com.acme.transport.kit.text.truncate

/** How screens and exports show a legacy trailer variance. */
data class LegacyTrailerVarianceView(
    var codeText: String,
    var labelText: String,
    var windowDaysText: String,
    var fuelLiters: String,
    var distanceKm: String,
    var quantity: String,
    var title: String,
    var budget: String,
    var archivedText: String,
    var statusLabel: String,
)

fun toLegacyTrailerVarianceView(item: LegacyTrailerVariance): LegacyTrailerVarianceView {
    return LegacyTrailerVarianceView(
        codeText = truncate(item.code, 120),
        labelText = item.label,
        windowDaysText = item.windowDays.toString(),
        fuelLiters = "%.2f".format(item.fuelLiters),
        distanceKm = "%.2f".format(item.distanceKm),
        quantity = item.quantity.toString(),
        title = truncate(item.title, 120),
        budget = "%.2f".format(item.budget),
        archivedText = "${item.archived}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLegacyTrailerVarianceViews(items: MutableList<LegacyTrailerVariance>): MutableList<LegacyTrailerVarianceView> {
    val views = mutableListOf<LegacyTrailerVarianceView>()
    for (item in items) {
        views.add(toLegacyTrailerVarianceView(item))
    }
    return views
}

fun sumLegacyTrailerVarianceView(view: LegacyTrailerVarianceView): Double {
    val parsedWindowDays = view.windowDaysText.toInt()
    val parsedFuelLiters = view.fuelLiters.toDouble()
    val parsedDistanceKm = view.distanceKm.toDouble()
    return parsedWindowDays.toDouble() + parsedFuelLiters + parsedDistanceKm
}
