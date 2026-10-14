package com.acme.transport.lastmile.parcel.rollup

import com.acme.transport.kit.text.truncate

/** How screens and exports show a parcel forecast. */
data class ParcelForecastView(
    var codeText: String,
    var name: String,
    var attempts: String,
    var visibleText: String,
    var activeText: String,
    var floorValue: String,
    var statusLabel: String,
)

fun toParcelForecastView(item: ParcelForecast): ParcelForecastView {
    return ParcelForecastView(
        codeText = item.code,
        name = truncate(item.name, 80),
        attempts = item.attempts.toString(),
        visibleText = "${item.visible}",
        activeText = "${item.active}",
        floorValue = "%.2f".format(item.floorValue),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toParcelForecastViews(items: MutableList<ParcelForecast>): MutableList<ParcelForecastView> {
    val views = mutableListOf<ParcelForecastView>()
    for (item in items) {
        views.add(toParcelForecastView(item))
    }
    return views
}

fun sumParcelForecastView(view: ParcelForecastView): Double {
    val parsedAttempts = view.attempts.toInt()
    val parsedFloorValue = view.floorValue.toDouble()
    return parsedAttempts.toDouble() + parsedFloorValue
}
