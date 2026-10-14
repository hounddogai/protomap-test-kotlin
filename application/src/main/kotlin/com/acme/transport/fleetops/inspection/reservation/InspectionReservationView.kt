package com.acme.transport.fleetops.inspection.reservation

/** How screens and exports show a inspection reservation. */
data class InspectionReservationView(
    var code: String,
    var nameText: String,
    var timezone: String,
    var locale: String,
    var ownerId: String,
    var attemptsText: String,
    var source: String,
    var archived: String,
    var weight: String,
    var reviewer: String,
    var startsAtText: String,
    var statusLabel: String,
)

fun toInspectionReservationView(item: InspectionReservation): InspectionReservationView {
    return InspectionReservationView(
        code = item.code,
        nameText = item.name,
        timezone = item.timezone,
        locale = item.locale,
        ownerId = item.ownerId,
        attemptsText = item.attempts.toString(),
        source = item.source,
        archived = "${item.archived}",
        weight = "%.2f".format(item.weight),
        reviewer = item.reviewer,
        startsAtText = item.startsAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toInspectionReservationViews(items: MutableList<InspectionReservation>): MutableList<InspectionReservationView> {
    val views = mutableListOf<InspectionReservationView>()
    for (item in items) {
        views.add(toInspectionReservationView(item))
    }
    return views
}

fun sumInspectionReservationView(view: InspectionReservationView): Double {
    val parsedAttempts = view.attemptsText.toInt()
    val parsedWeight = view.weight.toDouble()
    val parsedStartsAt = view.startsAtText.toInt()
    return parsedAttempts.toDouble() + parsedWeight + parsedStartsAt.toDouble()
}
