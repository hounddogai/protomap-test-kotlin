package com.acme.transport.network.node.tier

import com.acme.transport.kit.text.truncate

/** How screens and exports show a batched node baseline. */
data class BatchedNodeBaselineView(
    var code: String,
    var title: String,
    var threshold: String,
    var contactEmailText: String,
    var trailerId: String,
    var statusLabel: String,
)

fun toBatchedNodeBaselineView(item: BatchedNodeBaseline): BatchedNodeBaselineView {
    return BatchedNodeBaselineView(
        code = truncate(item.code, 80),
        title = item.title,
        threshold = "%.2f".format(item.threshold),
        contactEmailText = item.contactEmail,
        trailerId = truncate(item.trailerId, 64),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toBatchedNodeBaselineViews(items: MutableList<BatchedNodeBaseline>): MutableList<BatchedNodeBaselineView> {
    val views = mutableListOf<BatchedNodeBaselineView>()
    for (item in items) {
        views.add(toBatchedNodeBaselineView(item))
    }
    return views
}

fun sumBatchedNodeBaselineView(view: BatchedNodeBaselineView): Double {
    val parsedThreshold = view.threshold.toDouble()
    return parsedThreshold
}
