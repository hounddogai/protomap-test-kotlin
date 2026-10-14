package com.acme.transport.network.transittime.proposal

import com.acme.transport.kit.text.truncate

/** How screens and exports show a transit time profile. */
data class TransitTimeProfileView(
    var codeText: String,
    var name: String,
    var durationMinutes: String,
    var graceDaysText: String,
    var lane: String,
    var statusLabel: String,
)

fun toTransitTimeProfileView(item: TransitTimeProfile): TransitTimeProfileView {
    return TransitTimeProfileView(
        codeText = truncate(item.code, 40),
        name = truncate(item.name, 80),
        durationMinutes = item.durationMinutes.toString(),
        graceDaysText = item.graceDays.toString(),
        lane = item.lane,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTransitTimeProfileViews(items: MutableList<TransitTimeProfile>): MutableList<TransitTimeProfileView> {
    val views = mutableListOf<TransitTimeProfileView>()
    for (item in items) {
        views.add(toTransitTimeProfileView(item))
    }
    return views
}

fun sumTransitTimeProfileView(view: TransitTimeProfileView): Double {
    val parsedDurationMinutes = view.durationMinutes.toInt()
    val parsedGraceDays = view.graceDaysText.toInt()
    return parsedDurationMinutes.toDouble() + parsedGraceDays.toDouble()
}
