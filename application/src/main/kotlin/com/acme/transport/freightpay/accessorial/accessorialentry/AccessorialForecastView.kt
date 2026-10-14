package com.acme.transport.freightpay.accessorial.accessorialentry

import com.acme.transport.kit.text.truncate

/** How screens and exports show a accessorial forecast. */
data class AccessorialForecastView(
    var codeText: String,
    var titleText: String,
    var expiresAt: String,
    var name: String,
    var ceiling: String,
    var checksumText: String,
    var fuelLitersText: String,
    var windowDays: String,
    var sequenceText: String,
    var routeCodeText: String,
    var dock: String,
    var statusLabel: String,
)

fun toAccessorialForecastView(item: AccessorialForecast): AccessorialForecastView {
    return AccessorialForecastView(
        codeText = item.code,
        titleText = item.title,
        expiresAt = item.expiresAt.toString(),
        name = item.name,
        ceiling = "%.2f".format(item.ceiling),
        checksumText = truncate(item.checksum, 80),
        fuelLitersText = "%.2f".format(item.fuelLiters),
        windowDays = item.windowDays.toString(),
        sequenceText = item.sequence.toString(),
        routeCodeText = item.routeCode,
        dock = truncate(item.dock, 120),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAccessorialForecastViews(items: MutableList<AccessorialForecast>): MutableList<AccessorialForecastView> {
    val views = mutableListOf<AccessorialForecastView>()
    for (item in items) {
        views.add(toAccessorialForecastView(item))
    }
    return views
}

fun sumAccessorialForecastView(view: AccessorialForecastView): Double {
    val parsedExpiresAt = view.expiresAt.toInt()
    val parsedCeiling = view.ceiling.toDouble()
    val parsedFuelLiters = view.fuelLitersText.toDouble()
    return parsedExpiresAt.toDouble() + parsedCeiling + parsedFuelLiters
}
