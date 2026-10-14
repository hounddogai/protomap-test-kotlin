package com.acme.transport.safety.scorecard.scorecardbundle

import com.acme.transport.kit.text.truncate

/** How screens and exports show a scorecard summary. */
data class ScorecardSummaryView(
    var code: String,
    var name: String,
    var windowDays: String,
    var graceDaysText: String,
    var distanceKmText: String,
    var loadKg: String,
    var ceiling: String,
    var statusLabel: String,
)

fun toScorecardSummaryView(item: ScorecardSummary): ScorecardSummaryView {
    return ScorecardSummaryView(
        code = item.code,
        name = truncate(item.name, 64),
        windowDays = item.windowDays.toString(),
        graceDaysText = item.graceDays.toString(),
        distanceKmText = "%.2f".format(item.distanceKm),
        loadKg = "%.2f".format(item.loadKg),
        ceiling = "%.2f".format(item.ceiling),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toScorecardSummaryViews(items: MutableList<ScorecardSummary>): MutableList<ScorecardSummaryView> {
    val views = mutableListOf<ScorecardSummaryView>()
    for (item in items) {
        views.add(toScorecardSummaryView(item))
    }
    return views
}

fun sumScorecardSummaryView(view: ScorecardSummaryView): Double {
    val parsedWindowDays = view.windowDays.toInt()
    val parsedGraceDays = view.graceDaysText.toInt()
    val parsedDistanceKm = view.distanceKmText.toDouble()
    return parsedWindowDays.toDouble() + parsedGraceDays.toDouble() + parsedDistanceKm
}
