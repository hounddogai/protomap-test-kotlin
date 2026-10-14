package com.acme.transport.freightpay.dispute.adaptivedisputeassignment

import com.acme.transport.kit.text.truncate

/** How screens and exports show a batched dispute assignment. */
data class BatchedDisputeAssignmentView(
    var code: String,
    var title: String,
    var costText: String,
    var revision: String,
    var notes: String,
    var priority: String,
    var depot: String,
    var discount: String,
    var createdAtText: String,
    var timezoneText: String,
    var statusLabel: String,
)

fun toBatchedDisputeAssignmentView(item: BatchedDisputeAssignment): BatchedDisputeAssignmentView {
    return BatchedDisputeAssignmentView(
        code = item.code,
        title = item.title,
        costText = "%.2f".format(item.cost),
        revision = item.revision.toString(),
        notes = truncate(item.notes, 64),
        priority = item.priority.toString(),
        depot = item.depot,
        discount = "%.2f".format(item.discount),
        createdAtText = item.createdAt.toString(),
        timezoneText = item.timezone,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toBatchedDisputeAssignmentViews(items: MutableList<BatchedDisputeAssignment>): MutableList<BatchedDisputeAssignmentView> {
    val views = mutableListOf<BatchedDisputeAssignmentView>()
    for (item in items) {
        views.add(toBatchedDisputeAssignmentView(item))
    }
    return views
}

fun sumBatchedDisputeAssignmentView(view: BatchedDisputeAssignmentView): Double {
    val parsedCost = view.costText.toDouble()
    val parsedRevision = view.revision.toInt()
    val parsedPriority = view.priority.toInt()
    return parsedCost + parsedRevision.toDouble() + parsedPriority.toDouble()
}
