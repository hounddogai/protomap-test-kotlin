package com.acme.transport.customs.inspection.inspectionplan

import com.acme.transport.kit.text.truncate

/** How screens and exports show a primary inspection reservation. */
data class PrimaryInspectionReservationView(
    var codeText: String,
    var label: String,
    var checksumText: String,
    var carrier: String,
    var startsAtText: String,
    var limit: String,
    var score: String,
    var notes: String,
    var statusLabel: String,
)

fun toPrimaryInspectionReservationView(item: PrimaryInspectionReservation): PrimaryInspectionReservationView {
    return PrimaryInspectionReservationView(
        codeText = item.code,
        label = item.label,
        checksumText = truncate(item.checksum, 80),
        carrier = item.carrier,
        startsAtText = item.startsAt.toString(),
        limit = item.limit.toString(),
        score = "%.2f".format(item.score),
        notes = item.notes,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPrimaryInspectionReservationViews(items: MutableList<PrimaryInspectionReservation>): MutableList<PrimaryInspectionReservationView> {
    val views = mutableListOf<PrimaryInspectionReservationView>()
    for (item in items) {
        views.add(toPrimaryInspectionReservationView(item))
    }
    return views
}

fun sumPrimaryInspectionReservationView(view: PrimaryInspectionReservationView): Double {
    val parsedStartsAt = view.startsAtText.toInt()
    val parsedLimit = view.limit.toInt()
    val parsedScore = view.score.toDouble()
    return parsedStartsAt.toDouble() + parsedLimit.toDouble() + parsedScore
}
