package com.acme.transport.network.transittime.request

import com.acme.transport.kit.text.truncate

/** How screens and exports show a transit time request. */
data class TransitTimeRequestView(
    var code: String,
    var titleText: String,
    var currency: String,
    var shiftCode: String,
    var ratioText: String,
    var distanceKm: String,
    var categoryText: String,
    var marginText: String,
    var statusLabel: String,
)

fun toTransitTimeRequestView(item: TransitTimeRequest): TransitTimeRequestView {
    return TransitTimeRequestView(
        code = item.code,
        titleText = item.title,
        currency = item.currency,
        shiftCode = truncate(item.shiftCode, 40),
        ratioText = "%.2f".format(item.ratio),
        distanceKm = "%.2f".format(item.distanceKm),
        categoryText = item.category,
        marginText = "%.2f".format(item.margin),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTransitTimeRequestViews(items: MutableList<TransitTimeRequest>): MutableList<TransitTimeRequestView> {
    val views = mutableListOf<TransitTimeRequestView>()
    for (item in items) {
        views.add(toTransitTimeRequestView(item))
    }
    return views
}

fun sumTransitTimeRequestView(view: TransitTimeRequestView): Double {
    val parsedRatio = view.ratioText.toDouble()
    val parsedDistanceKm = view.distanceKm.toDouble()
    val parsedMargin = view.marginText.toDouble()
    return parsedRatio + parsedDistanceKm + parsedMargin
}
