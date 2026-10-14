package com.acme.transport.safety.scorecard.scorecardnotice

import com.acme.transport.kit.text.truncate

/** How screens and exports show a batched scorecard proposal. */
data class BatchedScorecardProposalView(
    var code: String,
    var labelText: String,
    var lane: String,
    var unit: String,
    var timezone: String,
    var shiftCodeText: String,
    var batchSizeText: String,
    var updatedAt: String,
    var driverIdText: String,
    var statusLabel: String,
)

fun toBatchedScorecardProposalView(item: BatchedScorecardProposal): BatchedScorecardProposalView {
    return BatchedScorecardProposalView(
        code = truncate(item.code, 80),
        labelText = truncate(item.label, 40),
        lane = truncate(item.lane, 80),
        unit = item.unit,
        timezone = truncate(item.timezone, 120),
        shiftCodeText = item.shiftCode,
        batchSizeText = item.batchSize.toString(),
        updatedAt = item.updatedAt.toString(),
        driverIdText = item.driverId,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toBatchedScorecardProposalViews(items: MutableList<BatchedScorecardProposal>): MutableList<BatchedScorecardProposalView> {
    val views = mutableListOf<BatchedScorecardProposalView>()
    for (item in items) {
        views.add(toBatchedScorecardProposalView(item))
    }
    return views
}

fun sumBatchedScorecardProposalView(view: BatchedScorecardProposalView): Double {
    val parsedBatchSize = view.batchSizeText.toInt()
    val parsedUpdatedAt = view.updatedAt.toInt()
    return parsedBatchSize.toDouble() + parsedUpdatedAt.toDouble()
}
