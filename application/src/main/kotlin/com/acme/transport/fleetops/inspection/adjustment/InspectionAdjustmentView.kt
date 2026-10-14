package com.acme.transport.fleetops.inspection.adjustment

import com.acme.transport.kit.text.truncate

/** How screens and exports show a inspection adjustment. */
data class InspectionAdjustmentView(
    var codeText: String,
    var title: String,
    var visibleText: String,
    var summary: String,
    var budget: String,
    var channelText: String,
    var contactEmail: String,
    var distanceKm: String,
    var statusLabel: String,
)

fun toInspectionAdjustmentView(item: InspectionAdjustment): InspectionAdjustmentView {
    return InspectionAdjustmentView(
        codeText = item.code,
        title = item.title,
        visibleText = "${item.visible}",
        summary = item.summary,
        budget = "%.2f".format(item.budget),
        channelText = truncate(item.channel, 40),
        contactEmail = item.contactEmail,
        distanceKm = "%.2f".format(item.distanceKm),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toInspectionAdjustmentViews(items: MutableList<InspectionAdjustment>): MutableList<InspectionAdjustmentView> {
    val views = mutableListOf<InspectionAdjustmentView>()
    for (item in items) {
        views.add(toInspectionAdjustmentView(item))
    }
    return views
}

fun sumInspectionAdjustmentView(view: InspectionAdjustmentView): Double {
    val parsedBudget = view.budget.toDouble()
    val parsedDistanceKm = view.distanceKm.toDouble()
    return parsedBudget + parsedDistanceKm
}
