package com.acme.transport.customs.inspection.plan

import com.acme.transport.kit.text.truncate

/** How screens and exports show a pending inspection adjustment. */
data class PendingInspectionAdjustmentView(
    var code: String,
    var name: String,
    var durationMinutesText: String,
    var labelText: String,
    var depot: String,
    var threshold: String,
    var category: String,
    var archived: String,
    var active: String,
    var amountText: String,
    var trailerIdText: String,
    var statusLabel: String,
)

fun toPendingInspectionAdjustmentView(item: PendingInspectionAdjustment): PendingInspectionAdjustmentView {
    return PendingInspectionAdjustmentView(
        code = item.code,
        name = item.name,
        durationMinutesText = item.durationMinutes.toString(),
        labelText = truncate(item.label, 64),
        depot = truncate(item.depot, 64),
        threshold = "%.2f".format(item.threshold),
        category = item.category,
        archived = "${item.archived}",
        active = "${item.active}",
        amountText = "%.2f".format(item.amount),
        trailerIdText = item.trailerId,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPendingInspectionAdjustmentViews(items: MutableList<PendingInspectionAdjustment>): MutableList<PendingInspectionAdjustmentView> {
    val views = mutableListOf<PendingInspectionAdjustmentView>()
    for (item in items) {
        views.add(toPendingInspectionAdjustmentView(item))
    }
    return views
}

fun sumPendingInspectionAdjustmentView(view: PendingInspectionAdjustmentView): Double {
    val parsedDurationMinutes = view.durationMinutesText.toInt()
    val parsedThreshold = view.threshold.toDouble()
    val parsedAmount = view.amountText.toDouble()
    return parsedDurationMinutes.toDouble() + parsedThreshold + parsedAmount
}
