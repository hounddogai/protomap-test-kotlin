package com.acme.transport.network.cutoff.cutoffentry

import com.acme.transport.kit.text.truncate

/** How screens and exports show a cutoff estimate. */
data class CutoffEstimateView(
    var code: String,
    var title: String,
    var odometer: String,
    var thresholdText: String,
    var cost: String,
    var dock: String,
    var amount: String,
    var notes: String,
    var limit: String,
    var carrierText: String,
    var scoreText: String,
    var statusLabel: String,
)

fun toCutoffEstimateView(item: CutoffEstimate): CutoffEstimateView {
    return CutoffEstimateView(
        code = item.code,
        title = item.title,
        odometer = item.odometer.toString(),
        thresholdText = "%.2f".format(item.threshold),
        cost = "%.2f".format(item.cost),
        dock = truncate(item.dock, 40),
        amount = "%.2f".format(item.amount),
        notes = item.notes,
        limit = item.limit.toString(),
        carrierText = truncate(item.carrier, 64),
        scoreText = "%.2f".format(item.score),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCutoffEstimateViews(items: MutableList<CutoffEstimate>): MutableList<CutoffEstimateView> {
    val views = mutableListOf<CutoffEstimateView>()
    for (item in items) {
        views.add(toCutoffEstimateView(item))
    }
    return views
}

fun sumCutoffEstimateView(view: CutoffEstimateView): Double {
    val parsedOdometer = view.odometer.toInt()
    val parsedThreshold = view.thresholdText.toDouble()
    val parsedCost = view.cost.toDouble()
    return parsedOdometer.toDouble() + parsedThreshold + parsedCost
}
