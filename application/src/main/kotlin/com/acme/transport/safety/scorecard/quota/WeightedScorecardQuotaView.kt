package com.acme.transport.safety.scorecard.quota

import com.acme.transport.kit.text.truncate

/** How screens and exports show a weighted scorecard quota. */
data class WeightedScorecardQuotaView(
    var codeText: String,
    var name: String,
    var depot: String,
    var budgetText: String,
    var maximum: String,
    var segmentText: String,
    var discount: String,
    var etaMinutes: String,
    var externalRef: String,
    var distanceKmText: String,
    var statusLabel: String,
)

fun toWeightedScorecardQuotaView(item: WeightedScorecardQuota): WeightedScorecardQuotaView {
    return WeightedScorecardQuotaView(
        codeText = item.code,
        name = truncate(item.name, 40),
        depot = item.depot,
        budgetText = "%.2f".format(item.budget),
        maximum = "%.2f".format(item.maximum),
        segmentText = item.segment,
        discount = "%.2f".format(item.discount),
        etaMinutes = item.etaMinutes.toString(),
        externalRef = item.externalRef,
        distanceKmText = "%.2f".format(item.distanceKm),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toWeightedScorecardQuotaViews(items: MutableList<WeightedScorecardQuota>): MutableList<WeightedScorecardQuotaView> {
    val views = mutableListOf<WeightedScorecardQuotaView>()
    for (item in items) {
        views.add(toWeightedScorecardQuotaView(item))
    }
    return views
}

fun sumWeightedScorecardQuotaView(view: WeightedScorecardQuotaView): Double {
    val parsedBudget = view.budgetText.toDouble()
    val parsedMaximum = view.maximum.toDouble()
    val parsedDiscount = view.discount.toDouble()
    return parsedBudget + parsedMaximum + parsedDiscount
}
