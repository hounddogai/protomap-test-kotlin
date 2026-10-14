package com.acme.transport.terminal.traileryard.rollup

import com.acme.transport.kit.text.truncate

/** How screens and exports show a fallback trailer yard variance. */
data class FallbackTrailerYardVarianceView(
    var code: String,
    var title: String,
    var laneText: String,
    var windowDaysText: String,
    var summary: String,
    var batchSize: String,
    var minimum: String,
    var version: String,
    var etaMinutes: String,
    var amount: String,
    var statusLabel: String,
)

fun toFallbackTrailerYardVarianceView(item: FallbackTrailerYardVariance): FallbackTrailerYardVarianceView {
    return FallbackTrailerYardVarianceView(
        code = item.code,
        title = truncate(item.title, 64),
        laneText = item.lane,
        windowDaysText = item.windowDays.toString(),
        summary = item.summary,
        batchSize = item.batchSize.toString(),
        minimum = "%.2f".format(item.minimum),
        version = item.version.toString(),
        etaMinutes = item.etaMinutes.toString(),
        amount = "%.2f".format(item.amount),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toFallbackTrailerYardVarianceViews(items: MutableList<FallbackTrailerYardVariance>): MutableList<FallbackTrailerYardVarianceView> {
    val views = mutableListOf<FallbackTrailerYardVarianceView>()
    for (item in items) {
        views.add(toFallbackTrailerYardVarianceView(item))
    }
    return views
}

fun sumFallbackTrailerYardVarianceView(view: FallbackTrailerYardVarianceView): Double {
    val parsedWindowDays = view.windowDaysText.toInt()
    val parsedBatchSize = view.batchSize.toInt()
    val parsedMinimum = view.minimum.toDouble()
    return parsedWindowDays.toDouble() + parsedBatchSize.toDouble() + parsedMinimum
}
