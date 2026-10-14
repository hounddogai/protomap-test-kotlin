package com.acme.transport.emissions.carbonreport.tieredreportscore

import com.acme.transport.kit.text.truncate

/** How screens and exports show a seasonal carbon report estimate. */
data class SeasonalCarbonReportEstimateView(
    var code: String,
    var name: String,
    var attemptsText: String,
    var routeCode: String,
    var windowDaysText: String,
    var checksum: String,
    var ownerId: String,
    var segment: String,
    var statusLabel: String,
)

fun toSeasonalCarbonReportEstimateView(item: SeasonalCarbonReportEstimate): SeasonalCarbonReportEstimateView {
    return SeasonalCarbonReportEstimateView(
        code = item.code,
        name = item.name,
        attemptsText = item.attempts.toString(),
        routeCode = truncate(item.routeCode, 40),
        windowDaysText = item.windowDays.toString(),
        checksum = item.checksum,
        ownerId = truncate(item.ownerId, 80),
        segment = truncate(item.segment, 40),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toSeasonalCarbonReportEstimateViews(items: MutableList<SeasonalCarbonReportEstimate>): MutableList<SeasonalCarbonReportEstimateView> {
    val views = mutableListOf<SeasonalCarbonReportEstimateView>()
    for (item in items) {
        views.add(toSeasonalCarbonReportEstimateView(item))
    }
    return views
}

fun sumSeasonalCarbonReportEstimateView(view: SeasonalCarbonReportEstimateView): Double {
    val parsedAttempts = view.attemptsText.toInt()
    val parsedWindowDays = view.windowDaysText.toInt()
    return parsedAttempts.toDouble() + parsedWindowDays.toDouble()
}
