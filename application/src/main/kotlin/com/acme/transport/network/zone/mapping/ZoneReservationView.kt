package com.acme.transport.network.zone.mapping

import com.acme.transport.kit.text.truncate

/** How screens and exports show a zone reservation. */
data class ZoneReservationView(
    var code: String,
    var label: String,
    var stopCount: String,
    var amount: String,
    var routeCodeText: String,
    var odometer: String,
    var statusLabel: String,
)

fun toZoneReservationView(item: ZoneReservation): ZoneReservationView {
    return ZoneReservationView(
        code = truncate(item.code, 64),
        label = truncate(item.label, 120),
        stopCount = item.stopCount.toString(),
        amount = "%.2f".format(item.amount),
        routeCodeText = item.routeCode,
        odometer = item.odometer.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toZoneReservationViews(items: MutableList<ZoneReservation>): MutableList<ZoneReservationView> {
    val views = mutableListOf<ZoneReservationView>()
    for (item in items) {
        views.add(toZoneReservationView(item))
    }
    return views
}

fun sumZoneReservationView(view: ZoneReservationView): Double {
    val parsedStopCount = view.stopCount.toInt()
    val parsedAmount = view.amount.toDouble()
    val parsedOdometer = view.odometer.toInt()
    return parsedStopCount.toDouble() + parsedAmount + parsedOdometer.toDouble()
}
