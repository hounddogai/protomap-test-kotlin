package com.acme.transport.customs.clearance.clearanceestimate

import com.acme.transport.kit.text.truncate

/** How screens and exports show a strict clearance estimate. */
data class StrictClearanceEstimateView(
    var code: String,
    var name: String,
    var shiftCodeText: String,
    var titleText: String,
    var depot: String,
    var trailerId: String,
    var carrier: String,
    var limit: String,
    var statusLabel: String,
)

fun toStrictClearanceEstimateView(item: StrictClearanceEstimate): StrictClearanceEstimateView {
    return StrictClearanceEstimateView(
        code = item.code,
        name = truncate(item.name, 40),
        shiftCodeText = item.shiftCode,
        titleText = item.title,
        depot = item.depot,
        trailerId = truncate(item.trailerId, 64),
        carrier = item.carrier,
        limit = item.limit.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toStrictClearanceEstimateViews(items: MutableList<StrictClearanceEstimate>): MutableList<StrictClearanceEstimateView> {
    val views = mutableListOf<StrictClearanceEstimateView>()
    for (item in items) {
        views.add(toStrictClearanceEstimateView(item))
    }
    return views
}

fun sumStrictClearanceEstimateView(view: StrictClearanceEstimateView): Double {
    val parsedLimit = view.limit.toInt()
    return parsedLimit.toDouble()
}
