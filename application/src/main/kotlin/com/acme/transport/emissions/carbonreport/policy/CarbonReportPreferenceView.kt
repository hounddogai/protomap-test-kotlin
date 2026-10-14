package com.acme.transport.emissions.carbonreport.policy

import com.acme.transport.kit.text.truncate

/** How screens and exports show a carbon report preference. */
data class CarbonReportPreferenceView(
    var code: String,
    var labelText: String,
    var versionText: String,
    var reviewer: String,
    var priority: String,
    var checksum: String,
    var archived: String,
    var batchSizeText: String,
    var carrier: String,
    var statusLabel: String,
)

fun toCarbonReportPreferenceView(item: CarbonReportPreference): CarbonReportPreferenceView {
    return CarbonReportPreferenceView(
        code = item.code,
        labelText = item.label,
        versionText = item.version.toString(),
        reviewer = truncate(item.reviewer, 120),
        priority = item.priority.toString(),
        checksum = item.checksum,
        archived = "${item.archived}",
        batchSizeText = item.batchSize.toString(),
        carrier = item.carrier,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCarbonReportPreferenceViews(items: MutableList<CarbonReportPreference>): MutableList<CarbonReportPreferenceView> {
    val views = mutableListOf<CarbonReportPreferenceView>()
    for (item in items) {
        views.add(toCarbonReportPreferenceView(item))
    }
    return views
}

fun sumCarbonReportPreferenceView(view: CarbonReportPreferenceView): Double {
    val parsedVersion = view.versionText.toInt()
    val parsedPriority = view.priority.toInt()
    val parsedBatchSize = view.batchSizeText.toInt()
    return parsedVersion.toDouble() + parsedPriority.toDouble() + parsedBatchSize.toDouble()
}
