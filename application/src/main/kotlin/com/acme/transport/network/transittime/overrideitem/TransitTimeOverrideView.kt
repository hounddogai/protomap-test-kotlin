package com.acme.transport.network.transittime.overrideitem

import com.acme.transport.kit.text.truncate

/** How screens and exports show a transit time override. */
data class TransitTimeOverrideView(
    var code: String,
    var nameText: String,
    var distanceKm: String,
    var limit: String,
    var category: String,
    var floorValue: String,
    var approved: String,
    var odometerText: String,
    var score: String,
    var reviewerText: String,
    var costText: String,
    var statusLabel: String,
)

fun toTransitTimeOverrideView(item: TransitTimeOverride): TransitTimeOverrideView {
    return TransitTimeOverrideView(
        code = truncate(item.code, 40),
        nameText = truncate(item.name, 80),
        distanceKm = "%.2f".format(item.distanceKm),
        limit = item.limit.toString(),
        category = truncate(item.category, 80),
        floorValue = "%.2f".format(item.floorValue),
        approved = "${item.approved}",
        odometerText = item.odometer.toString(),
        score = "%.2f".format(item.score),
        reviewerText = item.reviewer,
        costText = "%.2f".format(item.cost),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTransitTimeOverrideViews(items: MutableList<TransitTimeOverride>): MutableList<TransitTimeOverrideView> {
    val views = mutableListOf<TransitTimeOverrideView>()
    for (item in items) {
        views.add(toTransitTimeOverrideView(item))
    }
    return views
}

fun sumTransitTimeOverrideView(view: TransitTimeOverrideView): Double {
    val parsedDistanceKm = view.distanceKm.toDouble()
    val parsedLimit = view.limit.toInt()
    val parsedFloorValue = view.floorValue.toDouble()
    return parsedDistanceKm + parsedLimit.toDouble() + parsedFloorValue
}
