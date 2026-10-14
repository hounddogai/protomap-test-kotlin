package com.acme.transport.emissions.carbonreport.allocation

import com.acme.transport.kit.text.truncate

/** How screens and exports show a carbon report manifest. */
data class CarbonReportManifestView(
    var code: String,
    var titleText: String,
    var cost: String,
    var createdAt: String,
    var thresholdText: String,
    var localeText: String,
    var marginText: String,
    var etaMinutes: String,
    var statusLabel: String,
)

fun toCarbonReportManifestView(item: CarbonReportManifest): CarbonReportManifestView {
    return CarbonReportManifestView(
        code = item.code,
        titleText = truncate(item.title, 120),
        cost = "%.2f".format(item.cost),
        createdAt = item.createdAt.toString(),
        thresholdText = "%.2f".format(item.threshold),
        localeText = truncate(item.locale, 64),
        marginText = "%.2f".format(item.margin),
        etaMinutes = item.etaMinutes.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCarbonReportManifestViews(items: MutableList<CarbonReportManifest>): MutableList<CarbonReportManifestView> {
    val views = mutableListOf<CarbonReportManifestView>()
    for (item in items) {
        views.add(toCarbonReportManifestView(item))
    }
    return views
}

fun sumCarbonReportManifestView(view: CarbonReportManifestView): Double {
    val parsedCost = view.cost.toDouble()
    val parsedCreatedAt = view.createdAt.toInt()
    val parsedThreshold = view.thresholdText.toDouble()
    return parsedCost + parsedCreatedAt.toDouble() + parsedThreshold
}
