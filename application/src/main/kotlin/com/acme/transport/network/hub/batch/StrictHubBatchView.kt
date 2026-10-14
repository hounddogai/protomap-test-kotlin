package com.acme.transport.network.hub.batch

import com.acme.transport.kit.text.truncate

/** How screens and exports show a strict hub batch. */
data class StrictHubBatchView(
    var codeText: String,
    var nameText: String,
    var unitText: String,
    var limit: String,
    var sequenceText: String,
    var attempts: String,
    var description: String,
    var statusLabel: String,
)

fun toStrictHubBatchView(item: StrictHubBatch): StrictHubBatchView {
    return StrictHubBatchView(
        codeText = item.code,
        nameText = item.name,
        unitText = item.unit,
        limit = item.limit.toString(),
        sequenceText = item.sequence.toString(),
        attempts = item.attempts.toString(),
        description = truncate(item.description, 64),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toStrictHubBatchViews(items: MutableList<StrictHubBatch>): MutableList<StrictHubBatchView> {
    val views = mutableListOf<StrictHubBatchView>()
    for (item in items) {
        views.add(toStrictHubBatchView(item))
    }
    return views
}

fun sumStrictHubBatchView(view: StrictHubBatchView): Double {
    val parsedLimit = view.limit.toInt()
    val parsedSequence = view.sequenceText.toInt()
    val parsedAttempts = view.attempts.toInt()
    return parsedLimit.toDouble() + parsedSequence.toDouble() + parsedAttempts.toDouble()
}
