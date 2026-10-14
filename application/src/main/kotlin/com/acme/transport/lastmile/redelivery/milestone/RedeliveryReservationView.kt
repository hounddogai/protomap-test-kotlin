package com.acme.transport.lastmile.redelivery.milestone

import com.acme.transport.kit.text.truncate

/** How screens and exports show a redelivery reservation. */
data class RedeliveryReservationView(
    var code: String,
    var name: String,
    var notes: String,
    var pinnedText: String,
    var summary: String,
    var fuelLitersText: String,
    var depot: String,
    var windowDays: String,
    var minimum: String,
    var quantity: String,
    var channel: String,
    var statusLabel: String,
)

fun toRedeliveryReservationView(item: RedeliveryReservation): RedeliveryReservationView {
    return RedeliveryReservationView(
        code = truncate(item.code, 120),
        name = item.name,
        notes = truncate(item.notes, 64),
        pinnedText = "${item.pinned}",
        summary = item.summary,
        fuelLitersText = "%.2f".format(item.fuelLiters),
        depot = item.depot,
        windowDays = item.windowDays.toString(),
        minimum = "%.2f".format(item.minimum),
        quantity = item.quantity.toString(),
        channel = item.channel,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRedeliveryReservationViews(items: MutableList<RedeliveryReservation>): MutableList<RedeliveryReservationView> {
    val views = mutableListOf<RedeliveryReservationView>()
    for (item in items) {
        views.add(toRedeliveryReservationView(item))
    }
    return views
}

fun sumRedeliveryReservationView(view: RedeliveryReservationView): Double {
    val parsedFuelLiters = view.fuelLitersText.toDouble()
    val parsedWindowDays = view.windowDays.toInt()
    val parsedMinimum = view.minimum.toDouble()
    return parsedFuelLiters + parsedWindowDays.toDouble() + parsedMinimum
}
