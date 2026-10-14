package com.acme.transport.crewing.roster.rosterforecast

import com.acme.transport.kit.text.truncate

/** How screens and exports show a lenient roster forecast. */
data class LenientRosterForecastView(
    var code: String,
    var nameText: String,
    var palletsText: String,
    var distanceKmText: String,
    var driverId: String,
    var maximumText: String,
    var locale: String,
    var statusLabel: String,
)

fun toLenientRosterForecastView(item: LenientRosterForecast): LenientRosterForecastView {
    return LenientRosterForecastView(
        code = item.code,
        nameText = item.name,
        palletsText = item.pallets.toString(),
        distanceKmText = "%.2f".format(item.distanceKm),
        driverId = truncate(item.driverId, 80),
        maximumText = "%.2f".format(item.maximum),
        locale = item.locale,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLenientRosterForecastViews(items: MutableList<LenientRosterForecast>): MutableList<LenientRosterForecastView> {
    val views = mutableListOf<LenientRosterForecastView>()
    for (item in items) {
        views.add(toLenientRosterForecastView(item))
    }
    return views
}

fun sumLenientRosterForecastView(view: LenientRosterForecastView): Double {
    val parsedPallets = view.palletsText.toInt()
    val parsedDistanceKm = view.distanceKmText.toDouble()
    val parsedMaximum = view.maximumText.toDouble()
    return parsedPallets.toDouble() + parsedDistanceKm + parsedMaximum
}
