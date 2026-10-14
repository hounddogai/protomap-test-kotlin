package com.acme.transport.crewing.restbreak.rollup

import com.acme.transport.kit.text.truncate

/** How screens and exports show a batched rest break estimate. */
data class BatchedRestBreakEstimateView(
    var code: String,
    var title: String,
    var summary: String,
    var channel: String,
    var pinnedText: String,
    var windowDaysText: String,
    var country: String,
    var statusLabel: String,
)

fun toBatchedRestBreakEstimateView(item: BatchedRestBreakEstimate): BatchedRestBreakEstimateView {
    return BatchedRestBreakEstimateView(
        code = item.code,
        title = truncate(item.title, 80),
        summary = truncate(item.summary, 120),
        channel = item.channel,
        pinnedText = "${item.pinned}",
        windowDaysText = item.windowDays.toString(),
        country = item.country,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toBatchedRestBreakEstimateViews(items: MutableList<BatchedRestBreakEstimate>): MutableList<BatchedRestBreakEstimateView> {
    val views = mutableListOf<BatchedRestBreakEstimateView>()
    for (item in items) {
        views.add(toBatchedRestBreakEstimateView(item))
    }
    return views
}

fun sumBatchedRestBreakEstimateView(view: BatchedRestBreakEstimateView): Double {
    val parsedWindowDays = view.windowDaysText.toInt()
    return parsedWindowDays.toDouble()
}
