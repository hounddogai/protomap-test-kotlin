package com.acme.transport.emissions.idling.target

/** How screens and exports show a idling reservation. */
data class IdlingReservationView(
    var code: String,
    var title: String,
    var odometer: String,
    var notes: String,
    var createdAt: String,
    var statusLabel: String,
)

fun toIdlingReservationView(item: IdlingReservation): IdlingReservationView {
    return IdlingReservationView(
        code = item.code,
        title = item.title,
        odometer = item.odometer.toString(),
        notes = item.notes,
        createdAt = item.createdAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toIdlingReservationViews(items: MutableList<IdlingReservation>): MutableList<IdlingReservationView> {
    val views = mutableListOf<IdlingReservationView>()
    for (item in items) {
        views.add(toIdlingReservationView(item))
    }
    return views
}

fun sumIdlingReservationView(view: IdlingReservationView): Double {
    val parsedOdometer = view.odometer.toInt()
    val parsedCreatedAt = view.createdAt.toInt()
    return parsedOdometer.toDouble() + parsedCreatedAt.toDouble()
}
