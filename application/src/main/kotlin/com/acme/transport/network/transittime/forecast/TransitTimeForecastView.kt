package com.acme.transport.network.transittime.forecast

import com.acme.transport.kit.text.truncate

/** How screens and exports show a transit time forecast. */
data class TransitTimeForecastView(
    var codeText: String,
    var label: String,
    var externalRefText: String,
    var routeCode: String,
    var trailerId: String,
    var etaMinutes: String,
    var statusLabel: String,
)

fun toTransitTimeForecastView(item: TransitTimeForecast): TransitTimeForecastView {
    return TransitTimeForecastView(
        codeText = truncate(item.code, 80),
        label = item.label,
        externalRefText = item.externalRef,
        routeCode = truncate(item.routeCode, 80),
        trailerId = item.trailerId,
        etaMinutes = item.etaMinutes.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTransitTimeForecastViews(items: MutableList<TransitTimeForecast>): MutableList<TransitTimeForecastView> {
    val views = mutableListOf<TransitTimeForecastView>()
    for (item in items) {
        views.add(toTransitTimeForecastView(item))
    }
    return views
}

fun sumTransitTimeForecastView(view: TransitTimeForecastView): Double {
    val parsedEtaMinutes = view.etaMinutes.toInt()
    return parsedEtaMinutes.toDouble()
}
