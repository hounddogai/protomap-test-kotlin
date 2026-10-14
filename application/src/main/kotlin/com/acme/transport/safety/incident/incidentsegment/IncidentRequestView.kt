package com.acme.transport.safety.incident.incidentsegment

import com.acme.transport.kit.text.truncate

/** How screens and exports show a incident request. */
data class IncidentRequestView(
    var codeText: String,
    var title: String,
    var phoneText: String,
    var distanceKmText: String,
    var windowDays: String,
    var statusLabel: String,
)

fun toIncidentRequestView(item: IncidentRequest): IncidentRequestView {
    return IncidentRequestView(
        codeText = item.code,
        title = truncate(item.title, 40),
        phoneText = item.phone,
        distanceKmText = "%.2f".format(item.distanceKm),
        windowDays = item.windowDays.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toIncidentRequestViews(items: MutableList<IncidentRequest>): MutableList<IncidentRequestView> {
    val views = mutableListOf<IncidentRequestView>()
    for (item in items) {
        views.add(toIncidentRequestView(item))
    }
    return views
}

fun sumIncidentRequestView(view: IncidentRequestView): Double {
    val parsedDistanceKm = view.distanceKmText.toDouble()
    val parsedWindowDays = view.windowDays.toInt()
    return parsedDistanceKm + parsedWindowDays.toDouble()
}
