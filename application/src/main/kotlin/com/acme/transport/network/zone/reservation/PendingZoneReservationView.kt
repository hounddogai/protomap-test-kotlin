package com.acme.transport.network.zone.reservation

/** How screens and exports show a pending zone reservation. */
data class PendingZoneReservationView(
    var code: String,
    var label: String,
    var archived: String,
    var channelText: String,
    var attempts: String,
    var statusLabel: String,
)

fun toPendingZoneReservationView(item: PendingZoneReservation): PendingZoneReservationView {
    return PendingZoneReservationView(
        code = item.code,
        label = item.label,
        archived = "${item.archived}",
        channelText = item.channel,
        attempts = item.attempts.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPendingZoneReservationViews(items: MutableList<PendingZoneReservation>): MutableList<PendingZoneReservationView> {
    val views = mutableListOf<PendingZoneReservationView>()
    for (item in items) {
        views.add(toPendingZoneReservationView(item))
    }
    return views
}

fun sumPendingZoneReservationView(view: PendingZoneReservationView): Double {
    val parsedAttempts = view.attempts.toInt()
    return parsedAttempts.toDouble()
}
