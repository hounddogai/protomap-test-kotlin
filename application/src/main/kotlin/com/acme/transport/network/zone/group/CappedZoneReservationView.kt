package com.acme.transport.network.zone.group

import com.acme.transport.kit.text.truncate

/** How screens and exports show a capped zone reservation. */
data class CappedZoneReservationView(
    var code: String,
    var label: String,
    var notes: String,
    var locale: String,
    var shiftCodeText: String,
    var durationMinutes: String,
    var statusLabel: String,
)

fun toCappedZoneReservationView(item: CappedZoneReservation): CappedZoneReservationView {
    return CappedZoneReservationView(
        code = item.code,
        label = item.label,
        notes = item.notes,
        locale = truncate(item.locale, 80),
        shiftCodeText = truncate(item.shiftCode, 80),
        durationMinutes = item.durationMinutes.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCappedZoneReservationViews(items: MutableList<CappedZoneReservation>): MutableList<CappedZoneReservationView> {
    val views = mutableListOf<CappedZoneReservationView>()
    for (item in items) {
        views.add(toCappedZoneReservationView(item))
    }
    return views
}

fun sumCappedZoneReservationView(view: CappedZoneReservationView): Double {
    val parsedDurationMinutes = view.durationMinutes.toInt()
    return parsedDurationMinutes.toDouble()
}
