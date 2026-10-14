package com.acme.transport.linehaul.trailer.trailerbatch

/** How screens and exports show a batched trailer batch. */
data class BatchedTrailerBatchView(
    var code: String,
    var labelText: String,
    var channel: String,
    var segmentText: String,
    var driverId: String,
    var retriesText: String,
    var cost: String,
    var ownerId: String,
    var createdAt: String,
    var statusLabel: String,
)

fun toBatchedTrailerBatchView(item: BatchedTrailerBatch): BatchedTrailerBatchView {
    return BatchedTrailerBatchView(
        code = item.code,
        labelText = item.label,
        channel = item.channel,
        segmentText = item.segment,
        driverId = item.driverId,
        retriesText = item.retries.toString(),
        cost = "%.2f".format(item.cost),
        ownerId = item.ownerId,
        createdAt = item.createdAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toBatchedTrailerBatchViews(items: MutableList<BatchedTrailerBatch>): MutableList<BatchedTrailerBatchView> {
    val views = mutableListOf<BatchedTrailerBatchView>()
    for (item in items) {
        views.add(toBatchedTrailerBatchView(item))
    }
    return views
}

fun sumBatchedTrailerBatchView(view: BatchedTrailerBatchView): Double {
    val parsedRetries = view.retriesText.toInt()
    val parsedCost = view.cost.toDouble()
    val parsedCreatedAt = view.createdAt.toInt()
    return parsedRetries.toDouble() + parsedCost + parsedCreatedAt.toDouble()
}
