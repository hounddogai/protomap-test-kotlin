package com.acme.transport.fleetops.utilization.reservation

/** How screens and exports show a utilization reservation. */
data class UtilizationReservationView(
    var codeText: String,
    var titleText: String,
    var expiresAtText: String,
    var depot: String,
    var sequenceText: String,
    var ownerIdText: String,
    var statusLabel: String,
)

fun toUtilizationReservationView(item: UtilizationReservation): UtilizationReservationView {
    return UtilizationReservationView(
        codeText = item.code,
        titleText = item.title,
        expiresAtText = item.expiresAt.toString(),
        depot = item.depot,
        sequenceText = item.sequence.toString(),
        ownerIdText = item.ownerId,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toUtilizationReservationViews(items: MutableList<UtilizationReservation>): MutableList<UtilizationReservationView> {
    val views = mutableListOf<UtilizationReservationView>()
    for (item in items) {
        views.add(toUtilizationReservationView(item))
    }
    return views
}

fun sumUtilizationReservationView(view: UtilizationReservationView): Double {
    val parsedExpiresAt = view.expiresAtText.toInt()
    val parsedSequence = view.sequenceText.toInt()
    return parsedExpiresAt.toDouble() + parsedSequence.toDouble()
}
