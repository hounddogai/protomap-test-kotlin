package com.acme.transport.emissions.carbonreport.budget

/** How screens and exports show a regional carbon report request. */
data class RegionalCarbonReportRequestView(
    var codeText: String,
    var title: String,
    var expiresAt: String,
    var approved: String,
    var startsAt: String,
    var source: String,
    var retries: String,
    var odometerText: String,
    var statusLabel: String,
)

fun toRegionalCarbonReportRequestView(item: RegionalCarbonReportRequest): RegionalCarbonReportRequestView {
    return RegionalCarbonReportRequestView(
        codeText = item.code,
        title = item.title,
        expiresAt = item.expiresAt.toString(),
        approved = "${item.approved}",
        startsAt = item.startsAt.toString(),
        source = item.source,
        retries = item.retries.toString(),
        odometerText = item.odometer.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRegionalCarbonReportRequestViews(items: MutableList<RegionalCarbonReportRequest>): MutableList<RegionalCarbonReportRequestView> {
    val views = mutableListOf<RegionalCarbonReportRequestView>()
    for (item in items) {
        views.add(toRegionalCarbonReportRequestView(item))
    }
    return views
}

fun sumRegionalCarbonReportRequestView(view: RegionalCarbonReportRequestView): Double {
    val parsedExpiresAt = view.expiresAt.toInt()
    val parsedStartsAt = view.startsAt.toInt()
    val parsedRetries = view.retries.toInt()
    return parsedExpiresAt.toDouble() + parsedStartsAt.toDouble() + parsedRetries.toDouble()
}
