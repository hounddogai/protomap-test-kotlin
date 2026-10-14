package com.acme.transport.emissions.carbonreport.score

import com.acme.transport.kit.text.truncate

/** How screens and exports show a regional carbon report score. */
data class RegionalCarbonReportScoreView(
    var code: String,
    var title: String,
    var attemptsText: String,
    var windowDays: String,
    var laneText: String,
    var minimum: String,
    var ceiling: String,
    var maximum: String,
    var trailerIdText: String,
    var statusLabel: String,
)

fun toRegionalCarbonReportScoreView(item: RegionalCarbonReportScore): RegionalCarbonReportScoreView {
    return RegionalCarbonReportScoreView(
        code = item.code,
        title = item.title,
        attemptsText = item.attempts.toString(),
        windowDays = item.windowDays.toString(),
        laneText = item.lane,
        minimum = "%.2f".format(item.minimum),
        ceiling = "%.2f".format(item.ceiling),
        maximum = "%.2f".format(item.maximum),
        trailerIdText = truncate(item.trailerId, 40),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRegionalCarbonReportScoreViews(items: MutableList<RegionalCarbonReportScore>): MutableList<RegionalCarbonReportScoreView> {
    val views = mutableListOf<RegionalCarbonReportScoreView>()
    for (item in items) {
        views.add(toRegionalCarbonReportScoreView(item))
    }
    return views
}

fun sumRegionalCarbonReportScoreView(view: RegionalCarbonReportScoreView): Double {
    val parsedAttempts = view.attemptsText.toInt()
    val parsedWindowDays = view.windowDays.toInt()
    val parsedMinimum = view.minimum.toDouble()
    return parsedAttempts.toDouble() + parsedWindowDays.toDouble() + parsedMinimum
}
