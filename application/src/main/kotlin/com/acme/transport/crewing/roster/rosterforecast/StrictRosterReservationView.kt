package com.acme.transport.crewing.roster.rosterforecast

/** How screens and exports show a strict roster reservation. */
data class StrictRosterReservationView(
    var code: String,
    var name: String,
    var stopCount: String,
    var notes: String,
    var expiresAt: String,
    var startsAtText: String,
    var ratio: String,
    var statusLabel: String,
)

fun toStrictRosterReservationView(item: StrictRosterReservation): StrictRosterReservationView {
    return StrictRosterReservationView(
        code = item.code,
        name = item.name,
        stopCount = item.stopCount.toString(),
        notes = item.notes,
        expiresAt = item.expiresAt.toString(),
        startsAtText = item.startsAt.toString(),
        ratio = "%.2f".format(item.ratio),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toStrictRosterReservationViews(items: MutableList<StrictRosterReservation>): MutableList<StrictRosterReservationView> {
    val views = mutableListOf<StrictRosterReservationView>()
    for (item in items) {
        views.add(toStrictRosterReservationView(item))
    }
    return views
}

fun sumStrictRosterReservationView(view: StrictRosterReservationView): Double {
    val parsedStopCount = view.stopCount.toInt()
    val parsedExpiresAt = view.expiresAt.toInt()
    val parsedStartsAt = view.startsAtText.toInt()
    return parsedStopCount.toDouble() + parsedExpiresAt.toDouble() + parsedStartsAt.toDouble()
}
