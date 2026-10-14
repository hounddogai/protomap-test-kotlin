package com.acme.transport.emissions.carbonreport.adjustment

/** How screens and exports show a carbon report audit. */
data class CarbonReportAuditView(
    var code: String,
    var label: String,
    var stopCount: String,
    var expiresAt: String,
    var odometer: String,
    var lane: String,
    var statusLabel: String,
)

fun toCarbonReportAuditView(item: CarbonReportAudit): CarbonReportAuditView {
    return CarbonReportAuditView(
        code = item.code,
        label = item.label,
        stopCount = item.stopCount.toString(),
        expiresAt = item.expiresAt.toString(),
        odometer = item.odometer.toString(),
        lane = item.lane,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCarbonReportAuditViews(items: MutableList<CarbonReportAudit>): MutableList<CarbonReportAuditView> {
    val views = mutableListOf<CarbonReportAuditView>()
    for (item in items) {
        views.add(toCarbonReportAuditView(item))
    }
    return views
}

fun sumCarbonReportAuditView(view: CarbonReportAuditView): Double {
    val parsedStopCount = view.stopCount.toInt()
    val parsedExpiresAt = view.expiresAt.toInt()
    val parsedOdometer = view.odometer.toInt()
    return parsedStopCount.toDouble() + parsedExpiresAt.toDouble() + parsedOdometer.toDouble()
}
