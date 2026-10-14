package com.acme.transport.customs.inspection.allocation

import com.acme.transport.kit.text.truncate

/** How screens and exports show a inspection allocation. */
data class InspectionAllocationView(
    var codeText: String,
    var nameText: String,
    var currency: String,
    var priorityText: String,
    var dock: String,
    var ratioText: String,
    var retries: String,
    var region: String,
    var statusLabel: String,
)

fun toInspectionAllocationView(item: InspectionAllocation): InspectionAllocationView {
    return InspectionAllocationView(
        codeText = truncate(item.code, 40),
        nameText = item.name,
        currency = item.currency,
        priorityText = item.priority.toString(),
        dock = truncate(item.dock, 64),
        ratioText = "%.2f".format(item.ratio),
        retries = item.retries.toString(),
        region = item.region,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toInspectionAllocationViews(items: MutableList<InspectionAllocation>): MutableList<InspectionAllocationView> {
    val views = mutableListOf<InspectionAllocationView>()
    for (item in items) {
        views.add(toInspectionAllocationView(item))
    }
    return views
}

fun sumInspectionAllocationView(view: InspectionAllocationView): Double {
    val parsedPriority = view.priorityText.toInt()
    val parsedRatio = view.ratioText.toDouble()
    val parsedRetries = view.retries.toInt()
    return parsedPriority.toDouble() + parsedRatio + parsedRetries.toDouble()
}
