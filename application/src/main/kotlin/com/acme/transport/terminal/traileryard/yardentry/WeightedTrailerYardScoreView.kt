package com.acme.transport.terminal.traileryard.yardentry

import com.acme.transport.kit.text.truncate

/** How screens and exports show a weighted trailer yard score. */
data class WeightedTrailerYardScoreView(
    var codeText: String,
    var title: String,
    var source: String,
    var externalRefText: String,
    var dock: String,
    var amountText: String,
    var statusLabel: String,
)

fun toWeightedTrailerYardScoreView(item: WeightedTrailerYardScore): WeightedTrailerYardScoreView {
    return WeightedTrailerYardScoreView(
        codeText = item.code,
        title = item.title,
        source = item.source,
        externalRefText = truncate(item.externalRef, 64),
        dock = item.dock,
        amountText = "%.2f".format(item.amount),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toWeightedTrailerYardScoreViews(items: MutableList<WeightedTrailerYardScore>): MutableList<WeightedTrailerYardScoreView> {
    val views = mutableListOf<WeightedTrailerYardScoreView>()
    for (item in items) {
        views.add(toWeightedTrailerYardScoreView(item))
    }
    return views
}

fun sumWeightedTrailerYardScoreView(view: WeightedTrailerYardScoreView): Double {
    val parsedAmount = view.amountText.toDouble()
    return parsedAmount
}
