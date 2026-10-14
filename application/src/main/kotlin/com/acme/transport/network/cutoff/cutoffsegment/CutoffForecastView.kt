package com.acme.transport.network.cutoff.cutoffsegment

import com.acme.transport.kit.text.truncate

/** How screens and exports show a cutoff forecast. */
data class CutoffForecastView(
    var codeText: String,
    var title: String,
    var budget: String,
    var graceDays: String,
    var trailerIdText: String,
    var depot: String,
    var statusLabel: String,
)

fun toCutoffForecastView(item: CutoffForecast): CutoffForecastView {
    return CutoffForecastView(
        codeText = item.code,
        title = item.title,
        budget = "%.2f".format(item.budget),
        graceDays = item.graceDays.toString(),
        trailerIdText = item.trailerId,
        depot = truncate(item.depot, 120),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCutoffForecastViews(items: MutableList<CutoffForecast>): MutableList<CutoffForecastView> {
    val views = mutableListOf<CutoffForecastView>()
    for (item in items) {
        views.add(toCutoffForecastView(item))
    }
    return views
}

fun sumCutoffForecastView(view: CutoffForecastView): Double {
    val parsedBudget = view.budget.toDouble()
    val parsedGraceDays = view.graceDays.toInt()
    return parsedBudget + parsedGraceDays.toDouble()
}
