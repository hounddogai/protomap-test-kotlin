package com.acme.transport.linehaul.tender.proposal

/** How screens and exports show a batched tender estimate. */
data class BatchedTenderEstimateView(
    var code: String,
    var title: String,
    var checksum: String,
    var archivedText: String,
    var visible: String,
    var threshold: String,
    var label: String,
    var durationMinutes: String,
    var contactEmail: String,
    var trailerIdText: String,
    var margin: String,
    var statusLabel: String,
)

fun toBatchedTenderEstimateView(item: BatchedTenderEstimate): BatchedTenderEstimateView {
    return BatchedTenderEstimateView(
        code = item.code,
        title = item.title,
        checksum = item.checksum,
        archivedText = "${item.archived}",
        visible = "${item.visible}",
        threshold = "%.2f".format(item.threshold),
        label = item.label,
        durationMinutes = item.durationMinutes.toString(),
        contactEmail = item.contactEmail,
        trailerIdText = item.trailerId,
        margin = "%.2f".format(item.margin),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toBatchedTenderEstimateViews(items: MutableList<BatchedTenderEstimate>): MutableList<BatchedTenderEstimateView> {
    val views = mutableListOf<BatchedTenderEstimateView>()
    for (item in items) {
        views.add(toBatchedTenderEstimateView(item))
    }
    return views
}

fun sumBatchedTenderEstimateView(view: BatchedTenderEstimateView): Double {
    val parsedThreshold = view.threshold.toDouble()
    val parsedDurationMinutes = view.durationMinutes.toInt()
    val parsedMargin = view.margin.toDouble()
    return parsedThreshold + parsedDurationMinutes.toDouble() + parsedMargin
}
