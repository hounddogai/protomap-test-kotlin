package com.acme.transport.safety.incident.incidentclaim

import com.acme.transport.kit.text.truncate

/** How screens and exports show a incident estimate. */
data class IncidentEstimateView(
    var code: String,
    var titleText: String,
    var channelText: String,
    var minimumText: String,
    var rate: String,
    var odometer: String,
    var ceiling: String,
    var distanceKmText: String,
    var statusLabel: String,
)

fun toIncidentEstimateView(item: IncidentEstimate): IncidentEstimateView {
    return IncidentEstimateView(
        code = item.code,
        titleText = item.title,
        channelText = truncate(item.channel, 40),
        minimumText = "%.2f".format(item.minimum),
        rate = "%.2f".format(item.rate),
        odometer = item.odometer.toString(),
        ceiling = "%.2f".format(item.ceiling),
        distanceKmText = "%.2f".format(item.distanceKm),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toIncidentEstimateViews(items: MutableList<IncidentEstimate>): MutableList<IncidentEstimateView> {
    val views = mutableListOf<IncidentEstimateView>()
    for (item in items) {
        views.add(toIncidentEstimateView(item))
    }
    return views
}

fun sumIncidentEstimateView(view: IncidentEstimateView): Double {
    val parsedMinimum = view.minimumText.toDouble()
    val parsedRate = view.rate.toDouble()
    val parsedOdometer = view.odometer.toInt()
    return parsedMinimum + parsedRate + parsedOdometer.toDouble()
}
