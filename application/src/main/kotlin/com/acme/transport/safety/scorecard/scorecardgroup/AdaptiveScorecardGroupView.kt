package com.acme.transport.safety.scorecard.scorecardgroup

import com.acme.transport.kit.text.truncate

/** How screens and exports show a adaptive scorecard group. */
data class AdaptiveScorecardGroupView(
    var codeText: String,
    var title: String,
    var batchSizeText: String,
    var distanceKmText: String,
    var ceiling: String,
    var budgetText: String,
    var currencyText: String,
    var statusLabel: String,
)

fun toAdaptiveScorecardGroupView(item: AdaptiveScorecardGroup): AdaptiveScorecardGroupView {
    return AdaptiveScorecardGroupView(
        codeText = item.code,
        title = item.title,
        batchSizeText = item.batchSize.toString(),
        distanceKmText = "%.2f".format(item.distanceKm),
        ceiling = "%.2f".format(item.ceiling),
        budgetText = "%.2f".format(item.budget),
        currencyText = truncate(item.currency, 80),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAdaptiveScorecardGroupViews(items: MutableList<AdaptiveScorecardGroup>): MutableList<AdaptiveScorecardGroupView> {
    val views = mutableListOf<AdaptiveScorecardGroupView>()
    for (item in items) {
        views.add(toAdaptiveScorecardGroupView(item))
    }
    return views
}

fun sumAdaptiveScorecardGroupView(view: AdaptiveScorecardGroupView): Double {
    val parsedBatchSize = view.batchSizeText.toInt()
    val parsedDistanceKm = view.distanceKmText.toDouble()
    val parsedCeiling = view.ceiling.toDouble()
    return parsedBatchSize.toDouble() + parsedDistanceKm + parsedCeiling
}
