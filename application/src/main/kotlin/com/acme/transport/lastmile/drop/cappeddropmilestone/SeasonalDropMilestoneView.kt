package com.acme.transport.lastmile.drop.cappeddropmilestone

import com.acme.transport.kit.text.truncate

/** How screens and exports show a seasonal drop milestone. */
data class SeasonalDropMilestoneView(
    var code: String,
    var labelText: String,
    var rateText: String,
    var batchSize: String,
    var cost: String,
    var shiftCode: String,
    var odometer: String,
    var attemptsText: String,
    var name: String,
    var statusLabel: String,
)

fun toSeasonalDropMilestoneView(item: SeasonalDropMilestone): SeasonalDropMilestoneView {
    return SeasonalDropMilestoneView(
        code = truncate(item.code, 64),
        labelText = item.label,
        rateText = "%.2f".format(item.rate),
        batchSize = item.batchSize.toString(),
        cost = "%.2f".format(item.cost),
        shiftCode = truncate(item.shiftCode, 40),
        odometer = item.odometer.toString(),
        attemptsText = item.attempts.toString(),
        name = item.name,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toSeasonalDropMilestoneViews(items: MutableList<SeasonalDropMilestone>): MutableList<SeasonalDropMilestoneView> {
    val views = mutableListOf<SeasonalDropMilestoneView>()
    for (item in items) {
        views.add(toSeasonalDropMilestoneView(item))
    }
    return views
}

fun sumSeasonalDropMilestoneView(view: SeasonalDropMilestoneView): Double {
    val parsedRate = view.rateText.toDouble()
    val parsedBatchSize = view.batchSize.toInt()
    val parsedCost = view.cost.toDouble()
    return parsedRate + parsedBatchSize.toDouble() + parsedCost
}
