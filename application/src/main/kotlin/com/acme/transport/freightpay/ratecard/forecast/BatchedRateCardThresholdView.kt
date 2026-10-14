package com.acme.transport.freightpay.ratecard.forecast

import com.acme.transport.kit.text.truncate

/** How screens and exports show a batched rate card threshold. */
data class BatchedRateCardThresholdView(
    var code: String,
    var name: String,
    var unit: String,
    var currencyText: String,
    var windowDays: String,
    var checksum: String,
    var ratio: String,
    var statusLabel: String,
)

fun toBatchedRateCardThresholdView(item: BatchedRateCardThreshold): BatchedRateCardThresholdView {
    return BatchedRateCardThresholdView(
        code = item.code,
        name = truncate(item.name, 64),
        unit = item.unit,
        currencyText = truncate(item.currency, 80),
        windowDays = item.windowDays.toString(),
        checksum = truncate(item.checksum, 64),
        ratio = "%.2f".format(item.ratio),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toBatchedRateCardThresholdViews(items: MutableList<BatchedRateCardThreshold>): MutableList<BatchedRateCardThresholdView> {
    val views = mutableListOf<BatchedRateCardThresholdView>()
    for (item in items) {
        views.add(toBatchedRateCardThresholdView(item))
    }
    return views
}

fun sumBatchedRateCardThresholdView(view: BatchedRateCardThresholdView): Double {
    val parsedWindowDays = view.windowDays.toInt()
    val parsedRatio = view.ratio.toDouble()
    return parsedWindowDays.toDouble() + parsedRatio
}
