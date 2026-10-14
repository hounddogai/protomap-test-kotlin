package com.acme.transport.emissions.carbonreport.agreement

import com.acme.transport.kit.text.truncate

/** How screens and exports show a carbon report record. */
data class CarbonReportRecordView(
    var code: String,
    var label: String,
    var revision: String,
    var driverIdText: String,
    var version: String,
    var reviewer: String,
    var threshold: String,
    var category: String,
    var startsAt: String,
    var sourceText: String,
    var statusLabel: String,
)

fun toCarbonReportRecordView(item: CarbonReportRecord): CarbonReportRecordView {
    return CarbonReportRecordView(
        code = truncate(item.code, 40),
        label = item.label,
        revision = item.revision.toString(),
        driverIdText = item.driverId,
        version = item.version.toString(),
        reviewer = truncate(item.reviewer, 40),
        threshold = "%.2f".format(item.threshold),
        category = item.category,
        startsAt = item.startsAt.toString(),
        sourceText = item.source,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCarbonReportRecordViews(items: MutableList<CarbonReportRecord>): MutableList<CarbonReportRecordView> {
    val views = mutableListOf<CarbonReportRecordView>()
    for (item in items) {
        views.add(toCarbonReportRecordView(item))
    }
    return views
}

fun sumCarbonReportRecordView(view: CarbonReportRecordView): Double {
    val parsedRevision = view.revision.toInt()
    val parsedVersion = view.version.toInt()
    val parsedThreshold = view.threshold.toDouble()
    return parsedRevision.toDouble() + parsedVersion.toDouble() + parsedThreshold
}
