package com.acme.transport.emissions.carbonreport.tieredreportscore

import com.acme.transport.kit.text.truncate

/** How screens and exports show a carbon report bundle. */
data class CarbonReportBundleView(
    var code: String,
    var titleText: String,
    var channel: String,
    var sourceText: String,
    var floorValue: String,
    var shiftCodeText: String,
    var trailerIdText: String,
    var statusLabel: String,
)

fun toCarbonReportBundleView(item: CarbonReportBundle): CarbonReportBundleView {
    return CarbonReportBundleView(
        code = item.code,
        titleText = item.title,
        channel = item.channel,
        sourceText = truncate(item.source, 80),
        floorValue = "%.2f".format(item.floorValue),
        shiftCodeText = item.shiftCode,
        trailerIdText = item.trailerId,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCarbonReportBundleViews(items: MutableList<CarbonReportBundle>): MutableList<CarbonReportBundleView> {
    val views = mutableListOf<CarbonReportBundleView>()
    for (item in items) {
        views.add(toCarbonReportBundleView(item))
    }
    return views
}

fun sumCarbonReportBundleView(view: CarbonReportBundleView): Double {
    val parsedFloorValue = view.floorValue.toDouble()
    return parsedFloorValue
}
