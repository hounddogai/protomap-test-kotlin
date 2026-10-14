package com.acme.transport.linehaul.trailer.budget

import com.acme.transport.kit.text.truncate

/** How screens and exports show a adaptive trailer request. */
data class AdaptiveTrailerRequestView(
    var code: String,
    var name: String,
    var laneText: String,
    var timezone: String,
    var cost: String,
    var expiresAt: String,
    var floorValue: String,
    var pinned: String,
    var unit: String,
    var phoneText: String,
    var statusLabel: String,
)

fun toAdaptiveTrailerRequestView(item: AdaptiveTrailerRequest): AdaptiveTrailerRequestView {
    return AdaptiveTrailerRequestView(
        code = item.code,
        name = truncate(item.name, 80),
        laneText = item.lane,
        timezone = truncate(item.timezone, 40),
        cost = "%.2f".format(item.cost),
        expiresAt = item.expiresAt.toString(),
        floorValue = "%.2f".format(item.floorValue),
        pinned = "${item.pinned}",
        unit = item.unit,
        phoneText = item.phone,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAdaptiveTrailerRequestViews(items: MutableList<AdaptiveTrailerRequest>): MutableList<AdaptiveTrailerRequestView> {
    val views = mutableListOf<AdaptiveTrailerRequestView>()
    for (item in items) {
        views.add(toAdaptiveTrailerRequestView(item))
    }
    return views
}

fun sumAdaptiveTrailerRequestView(view: AdaptiveTrailerRequestView): Double {
    val parsedCost = view.cost.toDouble()
    val parsedExpiresAt = view.expiresAt.toInt()
    val parsedFloorValue = view.floorValue.toDouble()
    return parsedCost + parsedExpiresAt.toDouble() + parsedFloorValue
}
