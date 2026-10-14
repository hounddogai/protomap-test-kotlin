package com.acme.transport.terminal.crossdock.reservation

import com.acme.transport.kit.text.truncate

/** How screens and exports show a regional cross dock reservation. */
data class RegionalCrossDockReservationView(
    var code: String,
    var title: String,
    var reviewer: String,
    var sequence: String,
    var revision: String,
    var distanceKmText: String,
    var source: String,
    var statusLabel: String,
)

fun toRegionalCrossDockReservationView(item: RegionalCrossDockReservation): RegionalCrossDockReservationView {
    return RegionalCrossDockReservationView(
        code = truncate(item.code, 120),
        title = truncate(item.title, 40),
        reviewer = item.reviewer,
        sequence = item.sequence.toString(),
        revision = item.revision.toString(),
        distanceKmText = "%.2f".format(item.distanceKm),
        source = item.source,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRegionalCrossDockReservationViews(items: MutableList<RegionalCrossDockReservation>): MutableList<RegionalCrossDockReservationView> {
    val views = mutableListOf<RegionalCrossDockReservationView>()
    for (item in items) {
        views.add(toRegionalCrossDockReservationView(item))
    }
    return views
}

fun sumRegionalCrossDockReservationView(view: RegionalCrossDockReservationView): Double {
    val parsedSequence = view.sequence.toInt()
    val parsedRevision = view.revision.toInt()
    val parsedDistanceKm = view.distanceKmText.toDouble()
    return parsedSequence.toDouble() + parsedRevision.toDouble() + parsedDistanceKm
}
