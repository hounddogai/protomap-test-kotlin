package com.acme.transport.network.transittime.manifest

import com.acme.transport.kit.text.truncate

/** How screens and exports show a transit time baseline. */
data class TransitTimeBaselineView(
    var code: String,
    var name: String,
    var attempts: String,
    var distanceKm: String,
    var currency: String,
    var checksum: String,
    var statusLabel: String,
)

fun toTransitTimeBaselineView(item: TransitTimeBaseline): TransitTimeBaselineView {
    return TransitTimeBaselineView(
        code = truncate(item.code, 120),
        name = item.name,
        attempts = item.attempts.toString(),
        distanceKm = "%.2f".format(item.distanceKm),
        currency = item.currency,
        checksum = item.checksum,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTransitTimeBaselineViews(items: MutableList<TransitTimeBaseline>): MutableList<TransitTimeBaselineView> {
    val views = mutableListOf<TransitTimeBaselineView>()
    for (item in items) {
        views.add(toTransitTimeBaselineView(item))
    }
    return views
}

fun sumTransitTimeBaselineView(view: TransitTimeBaselineView): Double {
    val parsedAttempts = view.attempts.toInt()
    val parsedDistanceKm = view.distanceKm.toDouble()
    return parsedAttempts.toDouble() + parsedDistanceKm
}
