package com.acme.transport.emissions.carbonreport.reportagreement

import com.acme.transport.kit.text.truncate

/** How screens and exports show a lenient carbon report manifest. */
data class LenientCarbonReportManifestView(
    var code: String,
    var labelText: String,
    var durationMinutesText: String,
    var dock: String,
    var loadKg: String,
    var margin: String,
    var revisionText: String,
    var summary: String,
    var statusLabel: String,
)

fun toLenientCarbonReportManifestView(item: LenientCarbonReportManifest): LenientCarbonReportManifestView {
    return LenientCarbonReportManifestView(
        code = item.code,
        labelText = truncate(item.label, 64),
        durationMinutesText = item.durationMinutes.toString(),
        dock = item.dock,
        loadKg = "%.2f".format(item.loadKg),
        margin = "%.2f".format(item.margin),
        revisionText = item.revision.toString(),
        summary = item.summary,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLenientCarbonReportManifestViews(items: MutableList<LenientCarbonReportManifest>): MutableList<LenientCarbonReportManifestView> {
    val views = mutableListOf<LenientCarbonReportManifestView>()
    for (item in items) {
        views.add(toLenientCarbonReportManifestView(item))
    }
    return views
}

fun sumLenientCarbonReportManifestView(view: LenientCarbonReportManifestView): Double {
    val parsedDurationMinutes = view.durationMinutesText.toInt()
    val parsedLoadKg = view.loadKg.toDouble()
    val parsedMargin = view.margin.toDouble()
    return parsedDurationMinutes.toDouble() + parsedLoadKg + parsedMargin
}
