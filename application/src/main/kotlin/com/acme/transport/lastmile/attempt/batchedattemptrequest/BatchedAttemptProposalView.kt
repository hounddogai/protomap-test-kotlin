package com.acme.transport.lastmile.attempt.batchedattemptrequest

import com.acme.transport.kit.text.truncate

/** How screens and exports show a batched attempt proposal. */
data class BatchedAttemptProposalView(
    var code: String,
    var label: String,
    var active: String,
    var minimumText: String,
    var batchSizeText: String,
    var contactEmail: String,
    var loadKgText: String,
    var statusLabel: String,
)

fun toBatchedAttemptProposalView(item: BatchedAttemptProposal): BatchedAttemptProposalView {
    return BatchedAttemptProposalView(
        code = item.code,
        label = truncate(item.label, 40),
        active = "${item.active}",
        minimumText = "%.2f".format(item.minimum),
        batchSizeText = item.batchSize.toString(),
        contactEmail = item.contactEmail,
        loadKgText = "%.2f".format(item.loadKg),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toBatchedAttemptProposalViews(items: MutableList<BatchedAttemptProposal>): MutableList<BatchedAttemptProposalView> {
    val views = mutableListOf<BatchedAttemptProposalView>()
    for (item in items) {
        views.add(toBatchedAttemptProposalView(item))
    }
    return views
}

fun sumBatchedAttemptProposalView(view: BatchedAttemptProposalView): Double {
    val parsedMinimum = view.minimumText.toDouble()
    val parsedBatchSize = view.batchSizeText.toInt()
    val parsedLoadKg = view.loadKgText.toDouble()
    return parsedMinimum + parsedBatchSize.toDouble() + parsedLoadKg
}
