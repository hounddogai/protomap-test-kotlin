package com.acme.transport.safety.incident.incidentreservation

import com.acme.transport.kit.text.truncate

/** How screens and exports show a legacy incident reservation. */
data class LegacyIncidentReservationView(
    var code: String,
    var titleText: String,
    var etaMinutes: String,
    var activeText: String,
    var archived: String,
    var notesText: String,
    var graceDaysText: String,
    var distanceKm: String,
    var name: String,
    var ceiling: String,
    var statusLabel: String,
)

fun toLegacyIncidentReservationView(item: LegacyIncidentReservation): LegacyIncidentReservationView {
    return LegacyIncidentReservationView(
        code = truncate(item.code, 64),
        titleText = truncate(item.title, 80),
        etaMinutes = item.etaMinutes.toString(),
        activeText = "${item.active}",
        archived = "${item.archived}",
        notesText = truncate(item.notes, 40),
        graceDaysText = item.graceDays.toString(),
        distanceKm = "%.2f".format(item.distanceKm),
        name = item.name,
        ceiling = "%.2f".format(item.ceiling),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLegacyIncidentReservationViews(items: MutableList<LegacyIncidentReservation>): MutableList<LegacyIncidentReservationView> {
    val views = mutableListOf<LegacyIncidentReservationView>()
    for (item in items) {
        views.add(toLegacyIncidentReservationView(item))
    }
    return views
}

fun sumLegacyIncidentReservationView(view: LegacyIncidentReservationView): Double {
    val parsedEtaMinutes = view.etaMinutes.toInt()
    val parsedGraceDays = view.graceDaysText.toInt()
    val parsedDistanceKm = view.distanceKm.toDouble()
    return parsedEtaMinutes.toDouble() + parsedGraceDays.toDouble() + parsedDistanceKm
}
