package com.acme.transport.linehaul.trailer.summary

import com.acme.transport.kit.text.truncate

/** How screens and exports show a weighted trailer mapping. */
data class WeightedTrailerMappingView(
    var codeText: String,
    var title: String,
    var cost: String,
    var amountText: String,
    var archived: String,
    var scoreText: String,
    var lane: String,
    var statusLabel: String,
)

fun toWeightedTrailerMappingView(item: WeightedTrailerMapping): WeightedTrailerMappingView {
    return WeightedTrailerMappingView(
        codeText = item.code,
        title = item.title,
        cost = "%.2f".format(item.cost),
        amountText = "%.2f".format(item.amount),
        archived = "${item.archived}",
        scoreText = "%.2f".format(item.score),
        lane = truncate(item.lane, 80),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toWeightedTrailerMappingViews(items: MutableList<WeightedTrailerMapping>): MutableList<WeightedTrailerMappingView> {
    val views = mutableListOf<WeightedTrailerMappingView>()
    for (item in items) {
        views.add(toWeightedTrailerMappingView(item))
    }
    return views
}

fun sumWeightedTrailerMappingView(view: WeightedTrailerMappingView): Double {
    val parsedCost = view.cost.toDouble()
    val parsedAmount = view.amountText.toDouble()
    val parsedScore = view.scoreText.toDouble()
    return parsedCost + parsedAmount + parsedScore
}
