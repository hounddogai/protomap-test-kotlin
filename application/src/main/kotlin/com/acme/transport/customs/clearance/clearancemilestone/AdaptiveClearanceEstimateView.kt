package com.acme.transport.customs.clearance.clearancemilestone

import com.acme.transport.kit.text.truncate

/** How screens and exports show a adaptive clearance estimate. */
data class AdaptiveClearanceEstimateView(
    var code: String,
    var label: String,
    var cost: String,
    var shiftCode: String,
    var palletsText: String,
    var lane: String,
    var country: String,
    var reviewer: String,
    var distanceKm: String,
    var margin: String,
    var statusLabel: String,
)

fun toAdaptiveClearanceEstimateView(item: AdaptiveClearanceEstimate): AdaptiveClearanceEstimateView {
    return AdaptiveClearanceEstimateView(
        code = item.code,
        label = item.label,
        cost = "%.2f".format(item.cost),
        shiftCode = item.shiftCode,
        palletsText = item.pallets.toString(),
        lane = truncate(item.lane, 120),
        country = truncate(item.country, 120),
        reviewer = item.reviewer,
        distanceKm = "%.2f".format(item.distanceKm),
        margin = "%.2f".format(item.margin),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAdaptiveClearanceEstimateViews(items: MutableList<AdaptiveClearanceEstimate>): MutableList<AdaptiveClearanceEstimateView> {
    val views = mutableListOf<AdaptiveClearanceEstimateView>()
    for (item in items) {
        views.add(toAdaptiveClearanceEstimateView(item))
    }
    return views
}

fun sumAdaptiveClearanceEstimateView(view: AdaptiveClearanceEstimateView): Double {
    val parsedCost = view.cost.toDouble()
    val parsedPallets = view.palletsText.toInt()
    val parsedDistanceKm = view.distanceKm.toDouble()
    return parsedCost + parsedPallets.toDouble() + parsedDistanceKm
}
