package com.acme.transport.linehaul.backhaul.reservation

import com.acme.transport.kit.text.truncate

/** How screens and exports show a backhaul reservation. */
data class BackhaulReservationView(
    var code: String,
    var labelText: String,
    var archived: String,
    var budget: String,
    var maximum: String,
    var country: String,
    var channelText: String,
    var vehicleIdText: String,
    var windowDays: String,
    var startsAtText: String,
    var statusLabel: String,
)

fun toBackhaulReservationView(item: BackhaulReservation): BackhaulReservationView {
    return BackhaulReservationView(
        code = truncate(item.code, 40),
        labelText = item.label,
        archived = "${item.archived}",
        budget = "%.2f".format(item.budget),
        maximum = "%.2f".format(item.maximum),
        country = item.country,
        channelText = truncate(item.channel, 80),
        vehicleIdText = item.vehicleId,
        windowDays = item.windowDays.toString(),
        startsAtText = item.startsAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toBackhaulReservationViews(items: MutableList<BackhaulReservation>): MutableList<BackhaulReservationView> {
    val views = mutableListOf<BackhaulReservationView>()
    for (item in items) {
        views.add(toBackhaulReservationView(item))
    }
    return views
}

fun sumBackhaulReservationView(view: BackhaulReservationView): Double {
    val parsedBudget = view.budget.toDouble()
    val parsedMaximum = view.maximum.toDouble()
    val parsedWindowDays = view.windowDays.toInt()
    return parsedBudget + parsedMaximum + parsedWindowDays.toDouble()
}
