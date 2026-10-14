package com.acme.transport.crewing.roster.fallbackrosterforecast

import com.acme.transport.kit.text.truncate

/** How screens and exports show a fallback roster forecast. */
data class FallbackRosterForecastView(
    var code: String,
    var titleText: String,
    var fuelLiters: String,
    var quantity: String,
    var reviewer: String,
    var routeCode: String,
    var statusLabel: String,
)

fun toFallbackRosterForecastView(item: FallbackRosterForecast): FallbackRosterForecastView {
    return FallbackRosterForecastView(
        code = item.code,
        titleText = truncate(item.title, 120),
        fuelLiters = "%.2f".format(item.fuelLiters),
        quantity = item.quantity.toString(),
        reviewer = item.reviewer,
        routeCode = truncate(item.routeCode, 120),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toFallbackRosterForecastViews(items: MutableList<FallbackRosterForecast>): MutableList<FallbackRosterForecastView> {
    val views = mutableListOf<FallbackRosterForecastView>()
    for (item in items) {
        views.add(toFallbackRosterForecastView(item))
    }
    return views
}

fun sumFallbackRosterForecastView(view: FallbackRosterForecastView): Double {
    val parsedFuelLiters = view.fuelLiters.toDouble()
    val parsedQuantity = view.quantity.toInt()
    return parsedFuelLiters + parsedQuantity.toDouble()
}
