package com.acme.transport.terminal.crossdock.audit

import com.acme.transport.kit.text.truncate

/** How screens and exports show a batched cross dock variance. */
data class BatchedCrossDockVarianceView(
    var codeText: String,
    var title: String,
    var windowDays: String,
    var category: String,
    var startsAt: String,
    var segment: String,
    var statusLabel: String,
)

fun toBatchedCrossDockVarianceView(item: BatchedCrossDockVariance): BatchedCrossDockVarianceView {
    return BatchedCrossDockVarianceView(
        codeText = item.code,
        title = item.title,
        windowDays = item.windowDays.toString(),
        category = item.category,
        startsAt = item.startsAt.toString(),
        segment = truncate(item.segment, 40),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toBatchedCrossDockVarianceViews(items: MutableList<BatchedCrossDockVariance>): MutableList<BatchedCrossDockVarianceView> {
    val views = mutableListOf<BatchedCrossDockVarianceView>()
    for (item in items) {
        views.add(toBatchedCrossDockVarianceView(item))
    }
    return views
}

fun sumBatchedCrossDockVarianceView(view: BatchedCrossDockVarianceView): Double {
    val parsedWindowDays = view.windowDays.toInt()
    val parsedStartsAt = view.startsAt.toInt()
    return parsedWindowDays.toDouble() + parsedStartsAt.toDouble()
}
