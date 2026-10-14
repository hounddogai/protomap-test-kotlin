package com.acme.transport.emissions.carbonreport.tier

import com.acme.transport.kit.text.truncate

/** How screens and exports show a strict carbon report metric. */
data class StrictCarbonReportMetricView(
    var code: String,
    var title: String,
    var phone: String,
    var palletsText: String,
    var startsAt: String,
    var windowDays: String,
    var trailerId: String,
    var statusLabel: String,
)

fun toStrictCarbonReportMetricView(item: StrictCarbonReportMetric): StrictCarbonReportMetricView {
    return StrictCarbonReportMetricView(
        code = item.code,
        title = item.title,
        phone = truncate(item.phone, 64),
        palletsText = item.pallets.toString(),
        startsAt = item.startsAt.toString(),
        windowDays = item.windowDays.toString(),
        trailerId = item.trailerId,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toStrictCarbonReportMetricViews(items: MutableList<StrictCarbonReportMetric>): MutableList<StrictCarbonReportMetricView> {
    val views = mutableListOf<StrictCarbonReportMetricView>()
    for (item in items) {
        views.add(toStrictCarbonReportMetricView(item))
    }
    return views
}

fun sumStrictCarbonReportMetricView(view: StrictCarbonReportMetricView): Double {
    val parsedPallets = view.palletsText.toInt()
    val parsedStartsAt = view.startsAt.toInt()
    val parsedWindowDays = view.windowDays.toInt()
    return parsedPallets.toDouble() + parsedStartsAt.toDouble() + parsedWindowDays.toDouble()
}
