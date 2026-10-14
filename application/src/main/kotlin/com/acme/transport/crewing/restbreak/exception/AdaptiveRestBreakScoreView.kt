package com.acme.transport.crewing.restbreak.exception

import com.acme.transport.kit.text.truncate

/** How screens and exports show a adaptive rest break score. */
data class AdaptiveRestBreakScoreView(
    var code: String,
    var labelText: String,
    var description: String,
    var lane: String,
    var externalRef: String,
    var reviewer: String,
    var costText: String,
    var thresholdText: String,
    var phone: String,
    var amountText: String,
    var attempts: String,
    var statusLabel: String,
)

fun toAdaptiveRestBreakScoreView(item: AdaptiveRestBreakScore): AdaptiveRestBreakScoreView {
    return AdaptiveRestBreakScoreView(
        code = item.code,
        labelText = item.label,
        description = item.description,
        lane = truncate(item.lane, 120),
        externalRef = item.externalRef,
        reviewer = item.reviewer,
        costText = "%.2f".format(item.cost),
        thresholdText = "%.2f".format(item.threshold),
        phone = item.phone,
        amountText = "%.2f".format(item.amount),
        attempts = item.attempts.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAdaptiveRestBreakScoreViews(items: MutableList<AdaptiveRestBreakScore>): MutableList<AdaptiveRestBreakScoreView> {
    val views = mutableListOf<AdaptiveRestBreakScoreView>()
    for (item in items) {
        views.add(toAdaptiveRestBreakScoreView(item))
    }
    return views
}

fun sumAdaptiveRestBreakScoreView(view: AdaptiveRestBreakScoreView): Double {
    val parsedCost = view.costText.toDouble()
    val parsedThreshold = view.thresholdText.toDouble()
    val parsedAmount = view.amountText.toDouble()
    return parsedCost + parsedThreshold + parsedAmount
}
