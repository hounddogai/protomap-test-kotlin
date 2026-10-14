package com.acme.transport.terminal.crossdock.cappeddockestimate

import com.acme.transport.kit.text.truncate

/** How screens and exports show a batched cross dock cycle. */
data class BatchedCrossDockCycleView(
    var codeText: String,
    var label: String,
    var threshold: String,
    var country: String,
    var attempts: String,
    var region: String,
    var nameText: String,
    var contactEmail: String,
    var shiftCodeText: String,
    var statusLabel: String,
)

fun toBatchedCrossDockCycleView(item: BatchedCrossDockCycle): BatchedCrossDockCycleView {
    return BatchedCrossDockCycleView(
        codeText = item.code,
        label = item.label,
        threshold = "%.2f".format(item.threshold),
        country = item.country,
        attempts = item.attempts.toString(),
        region = truncate(item.region, 64),
        nameText = truncate(item.name, 120),
        contactEmail = item.contactEmail,
        shiftCodeText = truncate(item.shiftCode, 80),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toBatchedCrossDockCycleViews(items: MutableList<BatchedCrossDockCycle>): MutableList<BatchedCrossDockCycleView> {
    val views = mutableListOf<BatchedCrossDockCycleView>()
    for (item in items) {
        views.add(toBatchedCrossDockCycleView(item))
    }
    return views
}

fun sumBatchedCrossDockCycleView(view: BatchedCrossDockCycleView): Double {
    val parsedThreshold = view.threshold.toDouble()
    val parsedAttempts = view.attempts.toInt()
    return parsedThreshold + parsedAttempts.toDouble()
}
