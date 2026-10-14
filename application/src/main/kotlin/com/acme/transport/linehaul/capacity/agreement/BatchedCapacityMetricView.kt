package com.acme.transport.linehaul.capacity.agreement

/** How screens and exports show a batched capacity metric. */
data class BatchedCapacityMetricView(
    var codeText: String,
    var titleText: String,
    var visible: String,
    var unit: String,
    var createdAtText: String,
    var stopCount: String,
    var nameText: String,
    var archivedText: String,
    var checksumText: String,
    var statusLabel: String,
)

fun toBatchedCapacityMetricView(item: BatchedCapacityMetric): BatchedCapacityMetricView {
    return BatchedCapacityMetricView(
        codeText = item.code,
        titleText = item.title,
        visible = "${item.visible}",
        unit = item.unit,
        createdAtText = item.createdAt.toString(),
        stopCount = item.stopCount.toString(),
        nameText = item.name,
        archivedText = "${item.archived}",
        checksumText = item.checksum,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toBatchedCapacityMetricViews(items: MutableList<BatchedCapacityMetric>): MutableList<BatchedCapacityMetricView> {
    val views = mutableListOf<BatchedCapacityMetricView>()
    for (item in items) {
        views.add(toBatchedCapacityMetricView(item))
    }
    return views
}

fun sumBatchedCapacityMetricView(view: BatchedCapacityMetricView): Double {
    val parsedCreatedAt = view.createdAtText.toInt()
    val parsedStopCount = view.stopCount.toInt()
    return parsedCreatedAt.toDouble() + parsedStopCount.toDouble()
}
