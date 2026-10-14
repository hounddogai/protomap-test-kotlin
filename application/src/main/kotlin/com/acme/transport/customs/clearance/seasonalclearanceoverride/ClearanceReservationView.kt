package com.acme.transport.customs.clearance.seasonalclearanceoverride

import com.acme.transport.kit.text.truncate

/** How screens and exports show a clearance reservation. */
data class ClearanceReservationView(
    var code: String,
    var nameText: String,
    var notes: String,
    var startsAt: String,
    var weight: String,
    var windowDaysText: String,
    var ratio: String,
    var segment: String,
    var statusLabel: String,
)

fun toClearanceReservationView(item: ClearanceReservation): ClearanceReservationView {
    return ClearanceReservationView(
        code = item.code,
        nameText = item.name,
        notes = item.notes,
        startsAt = item.startsAt.toString(),
        weight = "%.2f".format(item.weight),
        windowDaysText = item.windowDays.toString(),
        ratio = "%.2f".format(item.ratio),
        segment = truncate(item.segment, 120),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toClearanceReservationViews(items: MutableList<ClearanceReservation>): MutableList<ClearanceReservationView> {
    val views = mutableListOf<ClearanceReservationView>()
    for (item in items) {
        views.add(toClearanceReservationView(item))
    }
    return views
}

fun sumClearanceReservationView(view: ClearanceReservationView): Double {
    val parsedStartsAt = view.startsAt.toInt()
    val parsedWeight = view.weight.toDouble()
    val parsedWindowDays = view.windowDaysText.toInt()
    return parsedStartsAt.toDouble() + parsedWeight + parsedWindowDays.toDouble()
}
