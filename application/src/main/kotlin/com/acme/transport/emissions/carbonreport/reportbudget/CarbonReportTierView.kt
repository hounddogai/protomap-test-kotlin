package com.acme.transport.emissions.carbonreport.reportbudget

import com.acme.transport.kit.text.truncate

/** How screens and exports show a carbon report tier. */
data class CarbonReportTierView(
    var codeText: String,
    var labelText: String,
    var durationMinutesText: String,
    var carrierText: String,
    var categoryText: String,
    var notes: String,
    var rate: String,
    var statusLabel: String,
)

fun toCarbonReportTierView(item: CarbonReportTier): CarbonReportTierView {
    return CarbonReportTierView(
        codeText = truncate(item.code, 64),
        labelText = item.label,
        durationMinutesText = item.durationMinutes.toString(),
        carrierText = item.carrier,
        categoryText = item.category,
        notes = item.notes,
        rate = "%.2f".format(item.rate),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCarbonReportTierViews(items: MutableList<CarbonReportTier>): MutableList<CarbonReportTierView> {
    val views = mutableListOf<CarbonReportTierView>()
    for (item in items) {
        views.add(toCarbonReportTierView(item))
    }
    return views
}

fun sumCarbonReportTierView(view: CarbonReportTierView): Double {
    val parsedDurationMinutes = view.durationMinutesText.toInt()
    val parsedRate = view.rate.toDouble()
    return parsedDurationMinutes.toDouble() + parsedRate
}
