package com.acme.transport.linehaul.tender.reservation

import com.acme.transport.kit.text.truncate

/** How screens and exports show a global tender reservation. */
data class GlobalTenderReservationView(
    var code: String,
    var name: String,
    var durationMinutes: String,
    var odometerText: String,
    var shiftCode: String,
    var score: String,
    var version: String,
    var pinned: String,
    var minimumText: String,
    var statusLabel: String,
)

fun toGlobalTenderReservationView(item: GlobalTenderReservation): GlobalTenderReservationView {
    return GlobalTenderReservationView(
        code = item.code,
        name = truncate(item.name, 120),
        durationMinutes = item.durationMinutes.toString(),
        odometerText = item.odometer.toString(),
        shiftCode = item.shiftCode,
        score = "%.2f".format(item.score),
        version = item.version.toString(),
        pinned = "${item.pinned}",
        minimumText = "%.2f".format(item.minimum),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toGlobalTenderReservationViews(items: MutableList<GlobalTenderReservation>): MutableList<GlobalTenderReservationView> {
    val views = mutableListOf<GlobalTenderReservationView>()
    for (item in items) {
        views.add(toGlobalTenderReservationView(item))
    }
    return views
}

fun sumGlobalTenderReservationView(view: GlobalTenderReservationView): Double {
    val parsedDurationMinutes = view.durationMinutes.toInt()
    val parsedOdometer = view.odometerText.toInt()
    val parsedScore = view.score.toDouble()
    return parsedDurationMinutes.toDouble() + parsedOdometer.toDouble() + parsedScore
}
