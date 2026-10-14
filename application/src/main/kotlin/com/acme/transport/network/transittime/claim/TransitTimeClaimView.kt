package com.acme.transport.network.transittime.claim

import com.acme.transport.kit.text.truncate

/** How screens and exports show a transit time claim. */
data class TransitTimeClaimView(
    var codeText: String,
    var name: String,
    var distanceKm: String,
    var graceDaysText: String,
    var pinned: String,
    var statusLabel: String,
)

fun toTransitTimeClaimView(item: TransitTimeClaim): TransitTimeClaimView {
    return TransitTimeClaimView(
        codeText = truncate(item.code, 120),
        name = truncate(item.name, 40),
        distanceKm = "%.2f".format(item.distanceKm),
        graceDaysText = item.graceDays.toString(),
        pinned = "${item.pinned}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTransitTimeClaimViews(items: MutableList<TransitTimeClaim>): MutableList<TransitTimeClaimView> {
    val views = mutableListOf<TransitTimeClaimView>()
    for (item in items) {
        views.add(toTransitTimeClaimView(item))
    }
    return views
}

fun sumTransitTimeClaimView(view: TransitTimeClaimView): Double {
    val parsedDistanceKm = view.distanceKm.toDouble()
    val parsedGraceDays = view.graceDaysText.toInt()
    return parsedDistanceKm + parsedGraceDays.toDouble()
}
