package com.acme.transport.emissions.fuelburn.burnbudget

import com.acme.transport.kit.text.truncate

/** How screens and exports show a fuel burn forecast. */
data class FuelBurnForecastView(
    var code: String,
    var title: String,
    var archivedText: String,
    var loadKg: String,
    var currencyText: String,
    var startsAt: String,
    var contactEmailText: String,
    var statusLabel: String,
)

fun toFuelBurnForecastView(item: FuelBurnForecast): FuelBurnForecastView {
    return FuelBurnForecastView(
        code = truncate(item.code, 80),
        title = item.title,
        archivedText = "${item.archived}",
        loadKg = "%.2f".format(item.loadKg),
        currencyText = item.currency,
        startsAt = item.startsAt.toString(),
        contactEmailText = item.contactEmail,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toFuelBurnForecastViews(items: MutableList<FuelBurnForecast>): MutableList<FuelBurnForecastView> {
    val views = mutableListOf<FuelBurnForecastView>()
    for (item in items) {
        views.add(toFuelBurnForecastView(item))
    }
    return views
}

fun sumFuelBurnForecastView(view: FuelBurnForecastView): Double {
    val parsedLoadKg = view.loadKg.toDouble()
    val parsedStartsAt = view.startsAt.toInt()
    return parsedLoadKg + parsedStartsAt.toDouble()
}
