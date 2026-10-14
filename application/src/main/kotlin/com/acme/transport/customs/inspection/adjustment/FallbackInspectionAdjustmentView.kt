package com.acme.transport.customs.inspection.adjustment

import com.acme.transport.kit.text.truncate

/** How screens and exports show a fallback inspection adjustment. */
data class FallbackInspectionAdjustmentView(
    var code: String,
    var name: String,
    var budget: String,
    var durationMinutesText: String,
    var batchSize: String,
    var versionText: String,
    var costText: String,
    var segment: String,
    var statusLabel: String,
)

fun toFallbackInspectionAdjustmentView(item: FallbackInspectionAdjustment): FallbackInspectionAdjustmentView {
    return FallbackInspectionAdjustmentView(
        code = truncate(item.code, 64),
        name = item.name,
        budget = "%.2f".format(item.budget),
        durationMinutesText = item.durationMinutes.toString(),
        batchSize = item.batchSize.toString(),
        versionText = item.version.toString(),
        costText = "%.2f".format(item.cost),
        segment = truncate(item.segment, 64),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toFallbackInspectionAdjustmentViews(items: MutableList<FallbackInspectionAdjustment>): MutableList<FallbackInspectionAdjustmentView> {
    val views = mutableListOf<FallbackInspectionAdjustmentView>()
    for (item in items) {
        views.add(toFallbackInspectionAdjustmentView(item))
    }
    return views
}

fun sumFallbackInspectionAdjustmentView(view: FallbackInspectionAdjustmentView): Double {
    val parsedBudget = view.budget.toDouble()
    val parsedDurationMinutes = view.durationMinutesText.toInt()
    val parsedBatchSize = view.batchSize.toInt()
    return parsedBudget + parsedDurationMinutes.toDouble() + parsedBatchSize.toDouble()
}
