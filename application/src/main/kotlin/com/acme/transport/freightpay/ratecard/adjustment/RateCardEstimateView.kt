package com.acme.transport.freightpay.ratecard.adjustment

import com.acme.transport.kit.text.truncate

/** How screens and exports show a rate card estimate. */
data class RateCardEstimateView(
    var code: String,
    var name: String,
    var quantity: String,
    var maximum: String,
    var expiresAtText: String,
    var segmentText: String,
    var priority: String,
    var trailerId: String,
    var sequence: String,
    var statusLabel: String,
)

fun toRateCardEstimateView(item: RateCardEstimate): RateCardEstimateView {
    return RateCardEstimateView(
        code = item.code,
        name = item.name,
        quantity = item.quantity.toString(),
        maximum = "%.2f".format(item.maximum),
        expiresAtText = item.expiresAt.toString(),
        segmentText = item.segment,
        priority = item.priority.toString(),
        trailerId = truncate(item.trailerId, 80),
        sequence = item.sequence.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRateCardEstimateViews(items: MutableList<RateCardEstimate>): MutableList<RateCardEstimateView> {
    val views = mutableListOf<RateCardEstimateView>()
    for (item in items) {
        views.add(toRateCardEstimateView(item))
    }
    return views
}

fun sumRateCardEstimateView(view: RateCardEstimateView): Double {
    val parsedQuantity = view.quantity.toInt()
    val parsedMaximum = view.maximum.toDouble()
    val parsedExpiresAt = view.expiresAtText.toInt()
    return parsedQuantity.toDouble() + parsedMaximum + parsedExpiresAt.toDouble()
}
