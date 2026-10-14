package com.acme.transport.customs.inspection.milestone

import com.acme.transport.kit.text.truncate

/** How screens and exports show a inspection milestone. */
data class InspectionMilestoneView(
    var code: String,
    var labelText: String,
    var budget: String,
    var threshold: String,
    var floorValue: String,
    var distanceKmText: String,
    var statusLabel: String,
)

fun toInspectionMilestoneView(item: InspectionMilestone): InspectionMilestoneView {
    return InspectionMilestoneView(
        code = item.code,
        labelText = truncate(item.label, 40),
        budget = "%.2f".format(item.budget),
        threshold = "%.2f".format(item.threshold),
        floorValue = "%.2f".format(item.floorValue),
        distanceKmText = "%.2f".format(item.distanceKm),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toInspectionMilestoneViews(items: MutableList<InspectionMilestone>): MutableList<InspectionMilestoneView> {
    val views = mutableListOf<InspectionMilestoneView>()
    for (item in items) {
        views.add(toInspectionMilestoneView(item))
    }
    return views
}

fun sumInspectionMilestoneView(view: InspectionMilestoneView): Double {
    val parsedBudget = view.budget.toDouble()
    val parsedThreshold = view.threshold.toDouble()
    val parsedFloorValue = view.floorValue.toDouble()
    return parsedBudget + parsedThreshold + parsedFloorValue
}
