package com.acme.transport.crewing.roster.tier

import com.acme.transport.kit.text.truncate

/** How screens and exports show a roster reservation. */
data class RosterReservationView(
    var codeText: String,
    var title: String,
    var depot: String,
    var retriesText: String,
    var stopCount: String,
    var label: String,
    var channel: String,
    var threshold: String,
    var limitText: String,
    var statusLabel: String,
)

fun toRosterReservationView(item: RosterReservation): RosterReservationView {
    return RosterReservationView(
        codeText = item.code,
        title = truncate(item.title, 120),
        depot = item.depot,
        retriesText = item.retries.toString(),
        stopCount = item.stopCount.toString(),
        label = item.label,
        channel = item.channel,
        threshold = "%.2f".format(item.threshold),
        limitText = item.limit.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRosterReservationViews(items: MutableList<RosterReservation>): MutableList<RosterReservationView> {
    val views = mutableListOf<RosterReservationView>()
    for (item in items) {
        views.add(toRosterReservationView(item))
    }
    return views
}

fun sumRosterReservationView(view: RosterReservationView): Double {
    val parsedRetries = view.retriesText.toInt()
    val parsedStopCount = view.stopCount.toInt()
    val parsedThreshold = view.threshold.toDouble()
    return parsedRetries.toDouble() + parsedStopCount.toDouble() + parsedThreshold
}
