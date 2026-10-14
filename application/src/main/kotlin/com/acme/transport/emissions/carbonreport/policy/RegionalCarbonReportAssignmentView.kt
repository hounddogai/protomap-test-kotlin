package com.acme.transport.emissions.carbonreport.policy

import com.acme.transport.kit.text.truncate

/** How screens and exports show a regional carbon report assignment. */
data class RegionalCarbonReportAssignmentView(
    var codeText: String,
    var name: String,
    var minimum: String,
    var budgetText: String,
    var windowDays: String,
    var sequence: String,
    var statusLabel: String,
)

fun toRegionalCarbonReportAssignmentView(item: RegionalCarbonReportAssignment): RegionalCarbonReportAssignmentView {
    return RegionalCarbonReportAssignmentView(
        codeText = item.code,
        name = truncate(item.name, 120),
        minimum = "%.2f".format(item.minimum),
        budgetText = "%.2f".format(item.budget),
        windowDays = item.windowDays.toString(),
        sequence = item.sequence.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRegionalCarbonReportAssignmentViews(items: MutableList<RegionalCarbonReportAssignment>): MutableList<RegionalCarbonReportAssignmentView> {
    val views = mutableListOf<RegionalCarbonReportAssignmentView>()
    for (item in items) {
        views.add(toRegionalCarbonReportAssignmentView(item))
    }
    return views
}

fun sumRegionalCarbonReportAssignmentView(view: RegionalCarbonReportAssignmentView): Double {
    val parsedMinimum = view.minimum.toDouble()
    val parsedBudget = view.budgetText.toDouble()
    val parsedWindowDays = view.windowDays.toInt()
    return parsedMinimum + parsedBudget + parsedWindowDays.toDouble()
}
