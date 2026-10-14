package com.acme.transport.linehaul.trailer.scheduledtrailerbatch

import com.acme.transport.kit.text.truncate

/** How screens and exports show a batched trailer score. */
data class BatchedTrailerScoreView(
    var codeText: String,
    var labelText: String,
    var cost: String,
    var revision: String,
    var windowDays: String,
    var statusLabel: String,
)

fun toBatchedTrailerScoreView(item: BatchedTrailerScore): BatchedTrailerScoreView {
    return BatchedTrailerScoreView(
        codeText = truncate(item.code, 64),
        labelText = item.label,
        cost = "%.2f".format(item.cost),
        revision = item.revision.toString(),
        windowDays = item.windowDays.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toBatchedTrailerScoreViews(items: MutableList<BatchedTrailerScore>): MutableList<BatchedTrailerScoreView> {
    val views = mutableListOf<BatchedTrailerScoreView>()
    for (item in items) {
        views.add(toBatchedTrailerScoreView(item))
    }
    return views
}

fun sumBatchedTrailerScoreView(view: BatchedTrailerScoreView): Double {
    val parsedCost = view.cost.toDouble()
    val parsedRevision = view.revision.toInt()
    val parsedWindowDays = view.windowDays.toInt()
    return parsedCost + parsedRevision.toDouble() + parsedWindowDays.toDouble()
}
