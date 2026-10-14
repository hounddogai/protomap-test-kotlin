package com.acme.transport.network.zone.reservation

/** How screens and exports show a batched zone segment. */
data class BatchedZoneSegmentView(
    var codeText: String,
    var nameText: String,
    var minimumText: String,
    var maximumText: String,
    var notes: String,
    var depotText: String,
    var category: String,
    var expiresAt: String,
    var statusLabel: String,
)

fun toBatchedZoneSegmentView(item: BatchedZoneSegment): BatchedZoneSegmentView {
    return BatchedZoneSegmentView(
        codeText = item.code,
        nameText = item.name,
        minimumText = "%.2f".format(item.minimum),
        maximumText = "%.2f".format(item.maximum),
        notes = item.notes,
        depotText = item.depot,
        category = item.category,
        expiresAt = item.expiresAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toBatchedZoneSegmentViews(items: MutableList<BatchedZoneSegment>): MutableList<BatchedZoneSegmentView> {
    val views = mutableListOf<BatchedZoneSegmentView>()
    for (item in items) {
        views.add(toBatchedZoneSegmentView(item))
    }
    return views
}

fun sumBatchedZoneSegmentView(view: BatchedZoneSegmentView): Double {
    val parsedMinimum = view.minimumText.toDouble()
    val parsedMaximum = view.maximumText.toDouble()
    val parsedExpiresAt = view.expiresAt.toInt()
    return parsedMinimum + parsedMaximum + parsedExpiresAt.toDouble()
}
