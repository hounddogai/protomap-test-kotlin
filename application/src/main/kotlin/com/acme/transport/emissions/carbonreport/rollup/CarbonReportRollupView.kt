package com.acme.transport.emissions.carbonreport.rollup

import com.acme.transport.kit.text.truncate

/** How screens and exports show a carbon report rollup. */
data class CarbonReportRollupView(
    var code: String,
    var label: String,
    var lane: String,
    var country: String,
    var source: String,
    var routeCode: String,
    var expiresAt: String,
    var summary: String,
    var statusLabel: String,
)

fun toCarbonReportRollupView(item: CarbonReportRollup): CarbonReportRollupView {
    return CarbonReportRollupView(
        code = item.code,
        label = item.label,
        lane = item.lane,
        country = truncate(item.country, 80),
        source = item.source,
        routeCode = truncate(item.routeCode, 120),
        expiresAt = item.expiresAt.toString(),
        summary = item.summary,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCarbonReportRollupViews(items: MutableList<CarbonReportRollup>): MutableList<CarbonReportRollupView> {
    val views = mutableListOf<CarbonReportRollupView>()
    for (item in items) {
        views.add(toCarbonReportRollupView(item))
    }
    return views
}

fun sumCarbonReportRollupView(view: CarbonReportRollupView): Double {
    val parsedExpiresAt = view.expiresAt.toInt()
    return parsedExpiresAt.toDouble()
}
