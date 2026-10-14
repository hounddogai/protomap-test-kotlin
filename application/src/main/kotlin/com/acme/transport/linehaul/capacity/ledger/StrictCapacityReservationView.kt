package com.acme.transport.linehaul.capacity.ledger

import com.acme.transport.kit.text.truncate

/** How screens and exports show a strict capacity reservation. */
data class StrictCapacityReservationView(
    var code: String,
    var nameText: String,
    var stopCount: String,
    var updatedAt: String,
    var summary: String,
    var margin: String,
    var amount: String,
    var score: String,
    var statusLabel: String,
)

fun toStrictCapacityReservationView(item: StrictCapacityReservation): StrictCapacityReservationView {
    return StrictCapacityReservationView(
        code = truncate(item.code, 80),
        nameText = truncate(item.name, 120),
        stopCount = item.stopCount.toString(),
        updatedAt = item.updatedAt.toString(),
        summary = item.summary,
        margin = "%.2f".format(item.margin),
        amount = "%.2f".format(item.amount),
        score = "%.2f".format(item.score),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toStrictCapacityReservationViews(items: MutableList<StrictCapacityReservation>): MutableList<StrictCapacityReservationView> {
    val views = mutableListOf<StrictCapacityReservationView>()
    for (item in items) {
        views.add(toStrictCapacityReservationView(item))
    }
    return views
}

fun sumStrictCapacityReservationView(view: StrictCapacityReservationView): Double {
    val parsedStopCount = view.stopCount.toInt()
    val parsedUpdatedAt = view.updatedAt.toInt()
    val parsedMargin = view.margin.toDouble()
    return parsedStopCount.toDouble() + parsedUpdatedAt.toDouble() + parsedMargin
}
