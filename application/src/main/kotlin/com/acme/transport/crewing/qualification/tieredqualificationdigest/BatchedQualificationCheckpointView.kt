package com.acme.transport.crewing.qualification.tieredqualificationdigest

import com.acme.transport.kit.text.truncate

/** How screens and exports show a batched qualification checkpoint. */
data class BatchedQualificationCheckpointView(
    var code: String,
    var title: String,
    var unit: String,
    var scoreText: String,
    var sourceText: String,
    var capacity: String,
    var statusLabel: String,
)

fun toBatchedQualificationCheckpointView(item: BatchedQualificationCheckpoint): BatchedQualificationCheckpointView {
    return BatchedQualificationCheckpointView(
        code = truncate(item.code, 80),
        title = item.title,
        unit = truncate(item.unit, 64),
        scoreText = "%.2f".format(item.score),
        sourceText = truncate(item.source, 40),
        capacity = item.capacity.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toBatchedQualificationCheckpointViews(items: MutableList<BatchedQualificationCheckpoint>): MutableList<BatchedQualificationCheckpointView> {
    val views = mutableListOf<BatchedQualificationCheckpointView>()
    for (item in items) {
        views.add(toBatchedQualificationCheckpointView(item))
    }
    return views
}

fun sumBatchedQualificationCheckpointView(view: BatchedQualificationCheckpointView): Double {
    val parsedScore = view.scoreText.toDouble()
    val parsedCapacity = view.capacity.toInt()
    return parsedScore + parsedCapacity.toDouble()
}
