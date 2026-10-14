package com.acme.transport.crewing.restbreak.agreement

import com.acme.transport.kit.text.truncate

/** How screens and exports show a rest break estimate. */
data class RestBreakEstimateView(
    var code: String,
    var name: String,
    var distanceKm: String,
    var currency: String,
    var reviewerText: String,
    var depot: String,
    var revision: String,
    var windowDays: String,
    var capacity: String,
    var statusLabel: String,
)

fun toRestBreakEstimateView(item: RestBreakEstimate): RestBreakEstimateView {
    return RestBreakEstimateView(
        code = item.code,
        name = item.name,
        distanceKm = "%.2f".format(item.distanceKm),
        currency = truncate(item.currency, 120),
        reviewerText = item.reviewer,
        depot = truncate(item.depot, 120),
        revision = item.revision.toString(),
        windowDays = item.windowDays.toString(),
        capacity = item.capacity.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRestBreakEstimateViews(items: MutableList<RestBreakEstimate>): MutableList<RestBreakEstimateView> {
    val views = mutableListOf<RestBreakEstimateView>()
    for (item in items) {
        views.add(toRestBreakEstimateView(item))
    }
    return views
}

fun sumRestBreakEstimateView(view: RestBreakEstimateView): Double {
    val parsedDistanceKm = view.distanceKm.toDouble()
    val parsedRevision = view.revision.toInt()
    val parsedWindowDays = view.windowDays.toInt()
    return parsedDistanceKm + parsedRevision.toDouble() + parsedWindowDays.toDouble()
}
