package com.acme.transport.emissions.carbonreport.rule

import com.acme.transport.kit.text.truncate

/** How screens and exports show a carbon report milestone. */
data class CarbonReportMilestoneView(
    var code: String,
    var labelText: String,
    var trailerIdText: String,
    var depotText: String,
    var discount: String,
    var graceDays: String,
    var floorValue: String,
    var version: String,
    var stopCount: String,
    var priorityText: String,
    var statusLabel: String,
)

fun toCarbonReportMilestoneView(item: CarbonReportMilestone): CarbonReportMilestoneView {
    return CarbonReportMilestoneView(
        code = item.code,
        labelText = truncate(item.label, 80),
        trailerIdText = item.trailerId,
        depotText = item.depot,
        discount = "%.2f".format(item.discount),
        graceDays = item.graceDays.toString(),
        floorValue = "%.2f".format(item.floorValue),
        version = item.version.toString(),
        stopCount = item.stopCount.toString(),
        priorityText = item.priority.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCarbonReportMilestoneViews(items: MutableList<CarbonReportMilestone>): MutableList<CarbonReportMilestoneView> {
    val views = mutableListOf<CarbonReportMilestoneView>()
    for (item in items) {
        views.add(toCarbonReportMilestoneView(item))
    }
    return views
}

fun sumCarbonReportMilestoneView(view: CarbonReportMilestoneView): Double {
    val parsedDiscount = view.discount.toDouble()
    val parsedGraceDays = view.graceDays.toInt()
    val parsedFloorValue = view.floorValue.toDouble()
    return parsedDiscount + parsedGraceDays.toDouble() + parsedFloorValue
}
