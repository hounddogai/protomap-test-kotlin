package com.acme.transport.emissions.evcharge.slot

import com.acme.transport.kit.text.truncate

/** How screens and exports show a ev charge forecast. */
data class EvChargeForecastView(
    var code: String,
    var title: String,
    var countryText: String,
    var expiresAt: String,
    var reviewer: String,
    var distanceKm: String,
    var timezone: String,
    var checksum: String,
    var amount: String,
    var statusLabel: String,
)

fun toEvChargeForecastView(item: EvChargeForecast): EvChargeForecastView {
    return EvChargeForecastView(
        code = truncate(item.code, 40),
        title = item.title,
        countryText = item.country,
        expiresAt = item.expiresAt.toString(),
        reviewer = item.reviewer,
        distanceKm = "%.2f".format(item.distanceKm),
        timezone = item.timezone,
        checksum = item.checksum,
        amount = "%.2f".format(item.amount),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toEvChargeForecastViews(items: MutableList<EvChargeForecast>): MutableList<EvChargeForecastView> {
    val views = mutableListOf<EvChargeForecastView>()
    for (item in items) {
        views.add(toEvChargeForecastView(item))
    }
    return views
}

fun sumEvChargeForecastView(view: EvChargeForecastView): Double {
    val parsedExpiresAt = view.expiresAt.toInt()
    val parsedDistanceKm = view.distanceKm.toDouble()
    val parsedAmount = view.amount.toDouble()
    return parsedExpiresAt.toDouble() + parsedDistanceKm + parsedAmount
}
