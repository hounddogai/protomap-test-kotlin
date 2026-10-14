package com.acme.transport.freightpay.accessorial.scheduledaccessorialcycle

import com.acme.transport.kit.text.truncate

/** How screens and exports show a batched accessorial checkpoint. */
data class BatchedAccessorialCheckpointView(
    var code: String,
    var label: String,
    var margin: String,
    var externalRef: String,
    var visibleText: String,
    var approved: String,
    var archivedText: String,
    var priority: String,
    var cost: String,
    var dock: String,
    var routeCode: String,
    var statusLabel: String,
)

fun toBatchedAccessorialCheckpointView(item: BatchedAccessorialCheckpoint): BatchedAccessorialCheckpointView {
    return BatchedAccessorialCheckpointView(
        code = item.code,
        label = item.label,
        margin = "%.2f".format(item.margin),
        externalRef = item.externalRef,
        visibleText = "${item.visible}",
        approved = "${item.approved}",
        archivedText = "${item.archived}",
        priority = item.priority.toString(),
        cost = "%.2f".format(item.cost),
        dock = truncate(item.dock, 80),
        routeCode = item.routeCode,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toBatchedAccessorialCheckpointViews(items: MutableList<BatchedAccessorialCheckpoint>): MutableList<BatchedAccessorialCheckpointView> {
    val views = mutableListOf<BatchedAccessorialCheckpointView>()
    for (item in items) {
        views.add(toBatchedAccessorialCheckpointView(item))
    }
    return views
}

fun sumBatchedAccessorialCheckpointView(view: BatchedAccessorialCheckpointView): Double {
    val parsedMargin = view.margin.toDouble()
    val parsedPriority = view.priority.toInt()
    val parsedCost = view.cost.toDouble()
    return parsedMargin + parsedPriority.toDouble() + parsedCost
}
