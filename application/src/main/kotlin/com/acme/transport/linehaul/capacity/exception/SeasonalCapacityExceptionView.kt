package com.acme.transport.linehaul.capacity.exception

import com.acme.transport.kit.text.truncate

/** How screens and exports show a seasonal capacity exception. */
data class SeasonalCapacityExceptionView(
    var code: String,
    var name: String,
    var distanceKm: String,
    var source: String,
    var weightText: String,
    var timezoneText: String,
    var rate: String,
    var currency: String,
    var reviewerText: String,
    var limitText: String,
    var routeCodeText: String,
    var statusLabel: String,
)

fun toSeasonalCapacityExceptionView(item: SeasonalCapacityException): SeasonalCapacityExceptionView {
    return SeasonalCapacityExceptionView(
        code = item.code,
        name = truncate(item.name, 40),
        distanceKm = "%.2f".format(item.distanceKm),
        source = item.source,
        weightText = "%.2f".format(item.weight),
        timezoneText = item.timezone,
        rate = "%.2f".format(item.rate),
        currency = truncate(item.currency, 64),
        reviewerText = truncate(item.reviewer, 64),
        limitText = item.limit.toString(),
        routeCodeText = truncate(item.routeCode, 40),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toSeasonalCapacityExceptionViews(items: MutableList<SeasonalCapacityException>): MutableList<SeasonalCapacityExceptionView> {
    val views = mutableListOf<SeasonalCapacityExceptionView>()
    for (item in items) {
        views.add(toSeasonalCapacityExceptionView(item))
    }
    return views
}

fun sumSeasonalCapacityExceptionView(view: SeasonalCapacityExceptionView): Double {
    val parsedDistanceKm = view.distanceKm.toDouble()
    val parsedWeight = view.weightText.toDouble()
    val parsedRate = view.rate.toDouble()
    return parsedDistanceKm + parsedWeight + parsedRate
}
