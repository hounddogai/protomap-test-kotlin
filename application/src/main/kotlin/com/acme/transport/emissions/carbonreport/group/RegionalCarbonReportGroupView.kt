package com.acme.transport.emissions.carbonreport.group

import com.acme.transport.kit.text.truncate

/** How screens and exports show a regional carbon report group. */
data class RegionalCarbonReportGroupView(
    var code: String,
    var name: String,
    var currency: String,
    var minimum: String,
    var labelText: String,
    var windowDays: String,
    var version: String,
    var amount: String,
    var sequenceText: String,
    var statusLabel: String,
)

fun toRegionalCarbonReportGroupView(item: RegionalCarbonReportGroup): RegionalCarbonReportGroupView {
    return RegionalCarbonReportGroupView(
        code = item.code,
        name = item.name,
        currency = truncate(item.currency, 64),
        minimum = "%.2f".format(item.minimum),
        labelText = truncate(item.label, 80),
        windowDays = item.windowDays.toString(),
        version = item.version.toString(),
        amount = "%.2f".format(item.amount),
        sequenceText = item.sequence.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRegionalCarbonReportGroupViews(items: MutableList<RegionalCarbonReportGroup>): MutableList<RegionalCarbonReportGroupView> {
    val views = mutableListOf<RegionalCarbonReportGroupView>()
    for (item in items) {
        views.add(toRegionalCarbonReportGroupView(item))
    }
    return views
}

fun sumRegionalCarbonReportGroupView(view: RegionalCarbonReportGroupView): Double {
    val parsedMinimum = view.minimum.toDouble()
    val parsedWindowDays = view.windowDays.toInt()
    val parsedVersion = view.version.toInt()
    return parsedMinimum + parsedWindowDays.toDouble() + parsedVersion.toDouble()
}
