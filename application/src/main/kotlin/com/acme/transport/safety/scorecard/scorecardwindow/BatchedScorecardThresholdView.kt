package com.acme.transport.safety.scorecard.scorecardwindow

import com.acme.transport.kit.text.truncate

/** How screens and exports show a batched scorecard threshold. */
data class BatchedScorecardThresholdView(
    var code: String,
    var name: String,
    var carrierText: String,
    var margin: String,
    var shiftCode: String,
    var revision: String,
    var sourceText: String,
    var palletsText: String,
    var batchSize: String,
    var statusLabel: String,
)

fun toBatchedScorecardThresholdView(item: BatchedScorecardThreshold): BatchedScorecardThresholdView {
    return BatchedScorecardThresholdView(
        code = item.code,
        name = truncate(item.name, 80),
        carrierText = truncate(item.carrier, 80),
        margin = "%.2f".format(item.margin),
        shiftCode = item.shiftCode,
        revision = item.revision.toString(),
        sourceText = item.source,
        palletsText = item.pallets.toString(),
        batchSize = item.batchSize.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toBatchedScorecardThresholdViews(items: MutableList<BatchedScorecardThreshold>): MutableList<BatchedScorecardThresholdView> {
    val views = mutableListOf<BatchedScorecardThresholdView>()
    for (item in items) {
        views.add(toBatchedScorecardThresholdView(item))
    }
    return views
}

fun sumBatchedScorecardThresholdView(view: BatchedScorecardThresholdView): Double {
    val parsedMargin = view.margin.toDouble()
    val parsedRevision = view.revision.toInt()
    val parsedPallets = view.palletsText.toInt()
    return parsedMargin + parsedRevision.toDouble() + parsedPallets.toDouble()
}
