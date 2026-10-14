package com.acme.transport.safety.incident.reservation

import com.acme.transport.kit.text.truncate

/** How screens and exports show a incident reservation. */
data class IncidentReservationView(
    var codeText: String,
    var titleText: String,
    var reviewer: String,
    var startsAtText: String,
    var stopCountText: String,
    var shiftCode: String,
    var categoryText: String,
    var region: String,
    var routeCode: String,
    var pinnedText: String,
    var segmentText: String,
    var statusLabel: String,
)

fun toIncidentReservationView(item: IncidentReservation): IncidentReservationView {
    return IncidentReservationView(
        codeText = item.code,
        titleText = item.title,
        reviewer = item.reviewer,
        startsAtText = item.startsAt.toString(),
        stopCountText = item.stopCount.toString(),
        shiftCode = item.shiftCode,
        categoryText = item.category,
        region = item.region,
        routeCode = truncate(item.routeCode, 120),
        pinnedText = "${item.pinned}",
        segmentText = truncate(item.segment, 120),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toIncidentReservationViews(items: MutableList<IncidentReservation>): MutableList<IncidentReservationView> {
    val views = mutableListOf<IncidentReservationView>()
    for (item in items) {
        views.add(toIncidentReservationView(item))
    }
    return views
}

fun sumIncidentReservationView(view: IncidentReservationView): Double {
    val parsedStartsAt = view.startsAtText.toInt()
    val parsedStopCount = view.stopCountText.toInt()
    return parsedStartsAt.toDouble() + parsedStopCount.toDouble()
}
