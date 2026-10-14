package com.acme.transport.emissions.carbonreport.digest

import com.acme.transport.kit.text.truncate

/** How screens and exports show a carbon report assignment. */
data class CarbonReportAssignmentView(
    var code: String,
    var name: String,
    var trailerId: String,
    var expiresAtText: String,
    var cost: String,
    var active: String,
    var currencyText: String,
    var windowDaysText: String,
    var statusLabel: String,
)

fun toCarbonReportAssignmentView(item: CarbonReportAssignment): CarbonReportAssignmentView {
    return CarbonReportAssignmentView(
        code = truncate(item.code, 80),
        name = item.name,
        trailerId = item.trailerId,
        expiresAtText = item.expiresAt.toString(),
        cost = "%.2f".format(item.cost),
        active = "${item.active}",
        currencyText = item.currency,
        windowDaysText = item.windowDays.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCarbonReportAssignmentViews(items: MutableList<CarbonReportAssignment>): MutableList<CarbonReportAssignmentView> {
    val views = mutableListOf<CarbonReportAssignmentView>()
    for (item in items) {
        views.add(toCarbonReportAssignmentView(item))
    }
    return views
}

fun sumCarbonReportAssignmentView(view: CarbonReportAssignmentView): Double {
    val parsedExpiresAt = view.expiresAtText.toInt()
    val parsedCost = view.cost.toDouble()
    val parsedWindowDays = view.windowDaysText.toInt()
    return parsedExpiresAt.toDouble() + parsedCost + parsedWindowDays.toDouble()
}
