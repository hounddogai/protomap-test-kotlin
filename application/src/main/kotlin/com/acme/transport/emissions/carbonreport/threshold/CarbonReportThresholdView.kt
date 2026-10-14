package com.acme.transport.emissions.carbonreport.threshold

import com.acme.transport.kit.text.truncate

/** How screens and exports show a carbon report threshold. */
data class CarbonReportThresholdView(
    var codeText: String,
    var titleText: String,
    var minimum: String,
    var windowDays: String,
    var regionText: String,
    var statusLabel: String,
)

fun toCarbonReportThresholdView(item: CarbonReportThreshold): CarbonReportThresholdView {
    return CarbonReportThresholdView(
        codeText = truncate(item.code, 80),
        titleText = truncate(item.title, 80),
        minimum = "%.2f".format(item.minimum),
        windowDays = item.windowDays.toString(),
        regionText = item.region,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCarbonReportThresholdViews(items: MutableList<CarbonReportThreshold>): MutableList<CarbonReportThresholdView> {
    val views = mutableListOf<CarbonReportThresholdView>()
    for (item in items) {
        views.add(toCarbonReportThresholdView(item))
    }
    return views
}

fun sumCarbonReportThresholdView(view: CarbonReportThresholdView): Double {
    val parsedMinimum = view.minimum.toDouble()
    val parsedWindowDays = view.windowDays.toInt()
    return parsedMinimum + parsedWindowDays.toDouble()
}
