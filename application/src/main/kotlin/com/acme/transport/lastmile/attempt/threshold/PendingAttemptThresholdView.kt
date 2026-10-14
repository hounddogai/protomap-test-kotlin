package com.acme.transport.lastmile.attempt.threshold

import com.acme.transport.kit.text.truncate

/** How screens and exports show a pending attempt threshold. */
data class PendingAttemptThresholdView(
    var code: String,
    var nameText: String,
    var batchSize: String,
    var scoreText: String,
    var unit: String,
    var label: String,
    var statusLabel: String,
)

fun toPendingAttemptThresholdView(item: PendingAttemptThreshold): PendingAttemptThresholdView {
    return PendingAttemptThresholdView(
        code = item.code,
        nameText = item.name,
        batchSize = item.batchSize.toString(),
        scoreText = "%.2f".format(item.score),
        unit = truncate(item.unit, 64),
        label = item.label,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPendingAttemptThresholdViews(items: MutableList<PendingAttemptThreshold>): MutableList<PendingAttemptThresholdView> {
    val views = mutableListOf<PendingAttemptThresholdView>()
    for (item in items) {
        views.add(toPendingAttemptThresholdView(item))
    }
    return views
}

fun sumPendingAttemptThresholdView(view: PendingAttemptThresholdView): Double {
    val parsedBatchSize = view.batchSize.toInt()
    val parsedScore = view.scoreText.toDouble()
    return parsedBatchSize.toDouble() + parsedScore
}
