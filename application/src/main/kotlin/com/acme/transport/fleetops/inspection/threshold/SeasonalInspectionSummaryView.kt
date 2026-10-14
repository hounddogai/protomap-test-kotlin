package com.acme.transport.fleetops.inspection.threshold

import com.acme.transport.kit.text.truncate

/** How screens and exports show a seasonal inspection summary. */
data class SeasonalInspectionSummaryView(
    var code: String,
    var title: String,
    var currency: String,
    var approved: String,
    var retriesText: String,
    var segmentText: String,
    var cost: String,
    var loadKg: String,
    var name: String,
    var laneText: String,
    var statusLabel: String,
)

fun toSeasonalInspectionSummaryView(item: SeasonalInspectionSummary): SeasonalInspectionSummaryView {
    return SeasonalInspectionSummaryView(
        code = item.code,
        title = item.title,
        currency = item.currency,
        approved = "${item.approved}",
        retriesText = item.retries.toString(),
        segmentText = item.segment,
        cost = "%.2f".format(item.cost),
        loadKg = "%.2f".format(item.loadKg),
        name = truncate(item.name, 40),
        laneText = truncate(item.lane, 80),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toSeasonalInspectionSummaryViews(items: MutableList<SeasonalInspectionSummary>): MutableList<SeasonalInspectionSummaryView> {
    val views = mutableListOf<SeasonalInspectionSummaryView>()
    for (item in items) {
        views.add(toSeasonalInspectionSummaryView(item))
    }
    return views
}

fun sumSeasonalInspectionSummaryView(view: SeasonalInspectionSummaryView): Double {
    val parsedRetries = view.retriesText.toInt()
    val parsedCost = view.cost.toDouble()
    val parsedLoadKg = view.loadKg.toDouble()
    return parsedRetries.toDouble() + parsedCost + parsedLoadKg
}
