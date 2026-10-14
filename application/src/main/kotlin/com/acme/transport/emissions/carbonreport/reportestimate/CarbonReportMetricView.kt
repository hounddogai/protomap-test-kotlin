package com.acme.transport.emissions.carbonreport.reportestimate

import com.acme.transport.kit.text.truncate

/** How screens and exports show a carbon report metric. */
data class CarbonReportMetricView(
    var codeText: String,
    var label: String,
    var startsAtText: String,
    var durationMinutes: String,
    var sourceText: String,
    var ceiling: String,
    var stopCountText: String,
    var segment: String,
    var carrier: String,
    var fuelLiters: String,
    var country: String,
    var statusLabel: String,
)

fun toCarbonReportMetricView(item: CarbonReportMetric): CarbonReportMetricView {
    return CarbonReportMetricView(
        codeText = truncate(item.code, 80),
        label = truncate(item.label, 40),
        startsAtText = item.startsAt.toString(),
        durationMinutes = item.durationMinutes.toString(),
        sourceText = truncate(item.source, 120),
        ceiling = "%.2f".format(item.ceiling),
        stopCountText = item.stopCount.toString(),
        segment = truncate(item.segment, 64),
        carrier = item.carrier,
        fuelLiters = "%.2f".format(item.fuelLiters),
        country = item.country,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCarbonReportMetricViews(items: MutableList<CarbonReportMetric>): MutableList<CarbonReportMetricView> {
    val views = mutableListOf<CarbonReportMetricView>()
    for (item in items) {
        views.add(toCarbonReportMetricView(item))
    }
    return views
}

fun sumCarbonReportMetricView(view: CarbonReportMetricView): Double {
    val parsedStartsAt = view.startsAtText.toInt()
    val parsedDurationMinutes = view.durationMinutes.toInt()
    val parsedCeiling = view.ceiling.toDouble()
    return parsedStartsAt.toDouble() + parsedDurationMinutes.toDouble() + parsedCeiling
}
