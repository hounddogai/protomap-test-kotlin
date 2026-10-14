package com.acme.transport.emissions.routeoffset.profile

import com.acme.transport.kit.text.truncate

/** How screens and exports show a route offset reservation. */
data class RouteOffsetReservationView(
    var code: String,
    var name: String,
    var limitText: String,
    var description: String,
    var sourceText: String,
    var loadKgText: String,
    var etaMinutes: String,
    var active: String,
    var currencyText: String,
    var retries: String,
    var vehicleIdText: String,
    var statusLabel: String,
)

fun toRouteOffsetReservationView(item: RouteOffsetReservation): RouteOffsetReservationView {
    return RouteOffsetReservationView(
        code = item.code,
        name = item.name,
        limitText = item.limit.toString(),
        description = item.description,
        sourceText = truncate(item.source, 80),
        loadKgText = "%.2f".format(item.loadKg),
        etaMinutes = item.etaMinutes.toString(),
        active = "${item.active}",
        currencyText = item.currency,
        retries = item.retries.toString(),
        vehicleIdText = item.vehicleId,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRouteOffsetReservationViews(items: MutableList<RouteOffsetReservation>): MutableList<RouteOffsetReservationView> {
    val views = mutableListOf<RouteOffsetReservationView>()
    for (item in items) {
        views.add(toRouteOffsetReservationView(item))
    }
    return views
}

fun sumRouteOffsetReservationView(view: RouteOffsetReservationView): Double {
    val parsedLimit = view.limitText.toInt()
    val parsedLoadKg = view.loadKgText.toDouble()
    val parsedEtaMinutes = view.etaMinutes.toInt()
    return parsedLimit.toDouble() + parsedLoadKg + parsedEtaMinutes.toDouble()
}
