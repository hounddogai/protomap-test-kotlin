package com.acme.transport.fleetops.leasereturn.proposal

import com.acme.transport.kit.text.truncate

/** How screens and exports show a lease return reservation. */
data class LeaseReturnReservationView(
    var codeText: String,
    var label: String,
    var depot: String,
    var loadKg: String,
    var stopCount: String,
    var vehicleId: String,
    var attemptsText: String,
    var localeText: String,
    var maximum: String,
    var updatedAt: String,
    var statusLabel: String,
)

fun toLeaseReturnReservationView(item: LeaseReturnReservation): LeaseReturnReservationView {
    return LeaseReturnReservationView(
        codeText = item.code,
        label = item.label,
        depot = item.depot,
        loadKg = "%.2f".format(item.loadKg),
        stopCount = item.stopCount.toString(),
        vehicleId = item.vehicleId,
        attemptsText = item.attempts.toString(),
        localeText = truncate(item.locale, 64),
        maximum = "%.2f".format(item.maximum),
        updatedAt = item.updatedAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLeaseReturnReservationViews(items: MutableList<LeaseReturnReservation>): MutableList<LeaseReturnReservationView> {
    val views = mutableListOf<LeaseReturnReservationView>()
    for (item in items) {
        views.add(toLeaseReturnReservationView(item))
    }
    return views
}

fun sumLeaseReturnReservationView(view: LeaseReturnReservationView): Double {
    val parsedLoadKg = view.loadKg.toDouble()
    val parsedStopCount = view.stopCount.toInt()
    val parsedAttempts = view.attemptsText.toInt()
    return parsedLoadKg + parsedStopCount.toDouble() + parsedAttempts.toDouble()
}
