package com.acme.transport.safety.scorecard.assignment

import com.acme.transport.kit.text.truncate

/** How screens and exports show a fallback scorecard target. */
data class FallbackScorecardTargetView(
    var code: String,
    var nameText: String,
    var startsAtText: String,
    var depot: String,
    var discount: String,
    var costText: String,
    var loadKgText: String,
    var currency: String,
    var distanceKm: String,
    var expiresAt: String,
    var label: String,
    var statusLabel: String,
)

fun toFallbackScorecardTargetView(item: FallbackScorecardTarget): FallbackScorecardTargetView {
    return FallbackScorecardTargetView(
        code = truncate(item.code, 40),
        nameText = item.name,
        startsAtText = item.startsAt.toString(),
        depot = truncate(item.depot, 64),
        discount = "%.2f".format(item.discount),
        costText = "%.2f".format(item.cost),
        loadKgText = "%.2f".format(item.loadKg),
        currency = item.currency,
        distanceKm = "%.2f".format(item.distanceKm),
        expiresAt = item.expiresAt.toString(),
        label = truncate(item.label, 40),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toFallbackScorecardTargetViews(items: MutableList<FallbackScorecardTarget>): MutableList<FallbackScorecardTargetView> {
    val views = mutableListOf<FallbackScorecardTargetView>()
    for (item in items) {
        views.add(toFallbackScorecardTargetView(item))
    }
    return views
}

fun sumFallbackScorecardTargetView(view: FallbackScorecardTargetView): Double {
    val parsedStartsAt = view.startsAtText.toInt()
    val parsedDiscount = view.discount.toDouble()
    val parsedCost = view.costText.toDouble()
    return parsedStartsAt.toDouble() + parsedDiscount + parsedCost
}
