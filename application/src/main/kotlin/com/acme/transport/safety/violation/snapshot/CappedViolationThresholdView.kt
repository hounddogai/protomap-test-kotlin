package com.acme.transport.safety.violation.snapshot

import com.acme.transport.kit.text.truncate

/** How screens and exports show a capped violation threshold. */
data class CappedViolationThresholdView(
    var codeText: String,
    var title: String,
    var sourceText: String,
    var threshold: String,
    var carrier: String,
    var ratio: String,
    var batchSize: String,
    var quantityText: String,
    var statusLabel: String,
)

fun toCappedViolationThresholdView(item: CappedViolationThreshold): CappedViolationThresholdView {
    return CappedViolationThresholdView(
        codeText = item.code,
        title = item.title,
        sourceText = item.source,
        threshold = "%.2f".format(item.threshold),
        carrier = truncate(item.carrier, 64),
        ratio = "%.2f".format(item.ratio),
        batchSize = item.batchSize.toString(),
        quantityText = item.quantity.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCappedViolationThresholdViews(items: MutableList<CappedViolationThreshold>): MutableList<CappedViolationThresholdView> {
    val views = mutableListOf<CappedViolationThresholdView>()
    for (item in items) {
        views.add(toCappedViolationThresholdView(item))
    }
    return views
}

fun sumCappedViolationThresholdView(view: CappedViolationThresholdView): Double {
    val parsedThreshold = view.threshold.toDouble()
    val parsedRatio = view.ratio.toDouble()
    val parsedBatchSize = view.batchSize.toInt()
    return parsedThreshold + parsedRatio + parsedBatchSize.toDouble()
}
