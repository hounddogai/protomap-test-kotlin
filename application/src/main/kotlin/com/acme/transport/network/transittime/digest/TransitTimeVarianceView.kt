package com.acme.transport.network.transittime.digest

import com.acme.transport.kit.text.truncate

/** How screens and exports show a transit time variance. */
data class TransitTimeVarianceView(
    var codeText: String,
    var title: String,
    var routeCode: String,
    var graceDays: String,
    var fuelLiters: String,
    var archivedText: String,
    var unitText: String,
    var marginText: String,
    var statusLabel: String,
)

fun toTransitTimeVarianceView(item: TransitTimeVariance): TransitTimeVarianceView {
    return TransitTimeVarianceView(
        codeText = item.code,
        title = truncate(item.title, 64),
        routeCode = truncate(item.routeCode, 40),
        graceDays = item.graceDays.toString(),
        fuelLiters = "%.2f".format(item.fuelLiters),
        archivedText = "${item.archived}",
        unitText = truncate(item.unit, 80),
        marginText = "%.2f".format(item.margin),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTransitTimeVarianceViews(items: MutableList<TransitTimeVariance>): MutableList<TransitTimeVarianceView> {
    val views = mutableListOf<TransitTimeVarianceView>()
    for (item in items) {
        views.add(toTransitTimeVarianceView(item))
    }
    return views
}

fun sumTransitTimeVarianceView(view: TransitTimeVarianceView): Double {
    val parsedGraceDays = view.graceDays.toInt()
    val parsedFuelLiters = view.fuelLiters.toDouble()
    val parsedMargin = view.marginText.toDouble()
    return parsedGraceDays.toDouble() + parsedFuelLiters + parsedMargin
}
