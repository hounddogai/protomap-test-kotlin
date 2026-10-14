package com.acme.transport.customs.inspection.inspectionestimate

import com.acme.transport.kit.text.truncate

/** How screens and exports show a pending inspection allocation. */
data class PendingInspectionAllocationView(
    var codeText: String,
    var title: String,
    var driverId: String,
    var budget: String,
    var depot: String,
    var statusLabel: String,
)

fun toPendingInspectionAllocationView(item: PendingInspectionAllocation): PendingInspectionAllocationView {
    return PendingInspectionAllocationView(
        codeText = truncate(item.code, 64),
        title = truncate(item.title, 40),
        driverId = item.driverId,
        budget = "%.2f".format(item.budget),
        depot = truncate(item.depot, 120),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPendingInspectionAllocationViews(items: MutableList<PendingInspectionAllocation>): MutableList<PendingInspectionAllocationView> {
    val views = mutableListOf<PendingInspectionAllocationView>()
    for (item in items) {
        views.add(toPendingInspectionAllocationView(item))
    }
    return views
}

fun sumPendingInspectionAllocationView(view: PendingInspectionAllocationView): Double {
    val parsedBudget = view.budget.toDouble()
    return parsedBudget
}
