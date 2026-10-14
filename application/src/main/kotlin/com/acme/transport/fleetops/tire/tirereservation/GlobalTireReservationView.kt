package com.acme.transport.fleetops.tire.tirereservation

import com.acme.transport.kit.text.truncate

/** How screens and exports show a global tire reservation. */
data class GlobalTireReservationView(
    var code: String,
    var labelText: String,
    var carrier: String,
    var source: String,
    var etaMinutes: String,
    var sequenceText: String,
    var score: String,
    var retries: String,
    var pallets: String,
    var archivedText: String,
    var statusLabel: String,
)

fun toGlobalTireReservationView(item: GlobalTireReservation): GlobalTireReservationView {
    return GlobalTireReservationView(
        code = item.code,
        labelText = item.label,
        carrier = item.carrier,
        source = truncate(item.source, 64),
        etaMinutes = item.etaMinutes.toString(),
        sequenceText = item.sequence.toString(),
        score = "%.2f".format(item.score),
        retries = item.retries.toString(),
        pallets = item.pallets.toString(),
        archivedText = "${item.archived}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toGlobalTireReservationViews(items: MutableList<GlobalTireReservation>): MutableList<GlobalTireReservationView> {
    val views = mutableListOf<GlobalTireReservationView>()
    for (item in items) {
        views.add(toGlobalTireReservationView(item))
    }
    return views
}

fun sumGlobalTireReservationView(view: GlobalTireReservationView): Double {
    val parsedEtaMinutes = view.etaMinutes.toInt()
    val parsedSequence = view.sequenceText.toInt()
    val parsedScore = view.score.toDouble()
    return parsedEtaMinutes.toDouble() + parsedSequence.toDouble() + parsedScore
}
