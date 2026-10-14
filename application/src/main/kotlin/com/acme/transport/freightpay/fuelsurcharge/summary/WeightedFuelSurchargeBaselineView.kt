package com.acme.transport.freightpay.fuelsurcharge.summary

import com.acme.transport.kit.text.truncate

/** How screens and exports show a weighted fuel surcharge baseline. */
data class WeightedFuelSurchargeBaselineView(
    var code: String,
    var name: String,
    var depot: String,
    var durationMinutesText: String,
    var stopCountText: String,
    var endsAtText: String,
    var expiresAtText: String,
    var versionText: String,
    var statusLabel: String,
)

fun toWeightedFuelSurchargeBaselineView(item: WeightedFuelSurchargeBaseline): WeightedFuelSurchargeBaselineView {
    return WeightedFuelSurchargeBaselineView(
        code = truncate(item.code, 64),
        name = item.name,
        depot = truncate(item.depot, 120),
        durationMinutesText = item.durationMinutes.toString(),
        stopCountText = item.stopCount.toString(),
        endsAtText = item.endsAt.toString(),
        expiresAtText = item.expiresAt.toString(),
        versionText = item.version.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toWeightedFuelSurchargeBaselineViews(items: MutableList<WeightedFuelSurchargeBaseline>): MutableList<WeightedFuelSurchargeBaselineView> {
    val views = mutableListOf<WeightedFuelSurchargeBaselineView>()
    for (item in items) {
        views.add(toWeightedFuelSurchargeBaselineView(item))
    }
    return views
}

fun sumWeightedFuelSurchargeBaselineView(view: WeightedFuelSurchargeBaselineView): Double {
    val parsedDurationMinutes = view.durationMinutesText.toInt()
    val parsedStopCount = view.stopCountText.toInt()
    val parsedEndsAt = view.endsAtText.toInt()
    return parsedDurationMinutes.toDouble() + parsedStopCount.toDouble() + parsedEndsAt.toDouble()
}
