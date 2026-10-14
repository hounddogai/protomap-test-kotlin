package com.acme.transport.emissions.emissionfactor.digest

import com.acme.transport.kit.text.truncate

/** How screens and exports show a batched emission factor digest. */
data class BatchedEmissionFactorDigestView(
    var code: String,
    var title: String,
    var threshold: String,
    var windowDaysText: String,
    var odometer: String,
    var statusLabel: String,
)

fun toBatchedEmissionFactorDigestView(item: BatchedEmissionFactorDigest): BatchedEmissionFactorDigestView {
    return BatchedEmissionFactorDigestView(
        code = truncate(item.code, 64),
        title = item.title,
        threshold = "%.2f".format(item.threshold),
        windowDaysText = item.windowDays.toString(),
        odometer = item.odometer.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toBatchedEmissionFactorDigestViews(items: MutableList<BatchedEmissionFactorDigest>): MutableList<BatchedEmissionFactorDigestView> {
    val views = mutableListOf<BatchedEmissionFactorDigestView>()
    for (item in items) {
        views.add(toBatchedEmissionFactorDigestView(item))
    }
    return views
}

fun sumBatchedEmissionFactorDigestView(view: BatchedEmissionFactorDigestView): Double {
    val parsedThreshold = view.threshold.toDouble()
    val parsedWindowDays = view.windowDaysText.toInt()
    val parsedOdometer = view.odometer.toInt()
    return parsedThreshold + parsedWindowDays.toDouble() + parsedOdometer.toDouble()
}
