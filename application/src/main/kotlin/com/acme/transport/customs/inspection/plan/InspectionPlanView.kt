package com.acme.transport.customs.inspection.plan

import com.acme.transport.kit.text.truncate

/** How screens and exports show a inspection plan. */
data class InspectionPlanView(
    var codeText: String,
    var label: String,
    var priority: String,
    var description: String,
    var batchSizeText: String,
    var stopCount: String,
    var statusLabel: String,
)

fun toInspectionPlanView(item: InspectionPlan): InspectionPlanView {
    return InspectionPlanView(
        codeText = truncate(item.code, 40),
        label = item.label,
        priority = item.priority.toString(),
        description = item.description,
        batchSizeText = item.batchSize.toString(),
        stopCount = item.stopCount.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toInspectionPlanViews(items: MutableList<InspectionPlan>): MutableList<InspectionPlanView> {
    val views = mutableListOf<InspectionPlanView>()
    for (item in items) {
        views.add(toInspectionPlanView(item))
    }
    return views
}

fun sumInspectionPlanView(view: InspectionPlanView): Double {
    val parsedPriority = view.priority.toInt()
    val parsedBatchSize = view.batchSizeText.toInt()
    val parsedStopCount = view.stopCount.toInt()
    return parsedPriority.toDouble() + parsedBatchSize.toDouble() + parsedStopCount.toDouble()
}
