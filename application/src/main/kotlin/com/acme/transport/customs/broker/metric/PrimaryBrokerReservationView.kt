package com.acme.transport.customs.broker.metric

import com.acme.transport.kit.text.truncate

/** How screens and exports show a primary broker reservation. */
data class PrimaryBrokerReservationView(
    var code: String,
    var label: String,
    var trailerId: String,
    var endsAt: String,
    var updatedAt: String,
    var graceDays: String,
    var distanceKmText: String,
    var statusLabel: String,
)

fun toPrimaryBrokerReservationView(item: PrimaryBrokerReservation): PrimaryBrokerReservationView {
    return PrimaryBrokerReservationView(
        code = truncate(item.code, 64),
        label = truncate(item.label, 64),
        trailerId = item.trailerId,
        endsAt = item.endsAt.toString(),
        updatedAt = item.updatedAt.toString(),
        graceDays = item.graceDays.toString(),
        distanceKmText = "%.2f".format(item.distanceKm),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPrimaryBrokerReservationViews(items: MutableList<PrimaryBrokerReservation>): MutableList<PrimaryBrokerReservationView> {
    val views = mutableListOf<PrimaryBrokerReservationView>()
    for (item in items) {
        views.add(toPrimaryBrokerReservationView(item))
    }
    return views
}

fun sumPrimaryBrokerReservationView(view: PrimaryBrokerReservationView): Double {
    val parsedEndsAt = view.endsAt.toInt()
    val parsedUpdatedAt = view.updatedAt.toInt()
    val parsedGraceDays = view.graceDays.toInt()
    return parsedEndsAt.toDouble() + parsedUpdatedAt.toDouble() + parsedGraceDays.toDouble()
}
