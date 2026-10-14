package com.acme.transport.terminal.crossdock.dockproposal

import com.acme.transport.kit.text.truncate

/** How screens and exports show a batched cross dock proposal. */
data class BatchedCrossDockProposalView(
    var code: String,
    var title: String,
    var ownerId: String,
    var capacity: String,
    var retries: String,
    var reviewer: String,
    var distanceKm: String,
    var revision: String,
    var statusLabel: String,
)

fun toBatchedCrossDockProposalView(item: BatchedCrossDockProposal): BatchedCrossDockProposalView {
    return BatchedCrossDockProposalView(
        code = truncate(item.code, 40),
        title = truncate(item.title, 64),
        ownerId = item.ownerId,
        capacity = item.capacity.toString(),
        retries = item.retries.toString(),
        reviewer = truncate(item.reviewer, 80),
        distanceKm = "%.2f".format(item.distanceKm),
        revision = item.revision.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toBatchedCrossDockProposalViews(items: MutableList<BatchedCrossDockProposal>): MutableList<BatchedCrossDockProposalView> {
    val views = mutableListOf<BatchedCrossDockProposalView>()
    for (item in items) {
        views.add(toBatchedCrossDockProposalView(item))
    }
    return views
}

fun sumBatchedCrossDockProposalView(view: BatchedCrossDockProposalView): Double {
    val parsedCapacity = view.capacity.toInt()
    val parsedRetries = view.retries.toInt()
    val parsedDistanceKm = view.distanceKm.toDouble()
    return parsedCapacity.toDouble() + parsedRetries.toDouble() + parsedDistanceKm
}
