package com.acme.transport.network.transittime.digest

import com.acme.transport.kit.text.truncate

/** How screens and exports show a transit time group. */
data class TransitTimeGroupView(
    var code: String,
    var nameText: String,
    var trailerId: String,
    var expiresAtText: String,
    var etaMinutesText: String,
    var statusLabel: String,
)

fun toTransitTimeGroupView(item: TransitTimeGroup): TransitTimeGroupView {
    return TransitTimeGroupView(
        code = truncate(item.code, 80),
        nameText = truncate(item.name, 120),
        trailerId = truncate(item.trailerId, 80),
        expiresAtText = item.expiresAt.toString(),
        etaMinutesText = item.etaMinutes.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTransitTimeGroupViews(items: MutableList<TransitTimeGroup>): MutableList<TransitTimeGroupView> {
    val views = mutableListOf<TransitTimeGroupView>()
    for (item in items) {
        views.add(toTransitTimeGroupView(item))
    }
    return views
}

fun sumTransitTimeGroupView(view: TransitTimeGroupView): Double {
    val parsedExpiresAt = view.expiresAtText.toInt()
    val parsedEtaMinutes = view.etaMinutesText.toInt()
    return parsedExpiresAt.toDouble() + parsedEtaMinutes.toDouble()
}
