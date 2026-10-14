package com.acme.transport.safety.scorecard.scorecardslot

import com.acme.transport.kit.text.truncate

/** How screens and exports show a batched scorecard bundle. */
data class BatchedScorecardBundleView(
    var code: String,
    var title: String,
    var margin: String,
    var windowDays: String,
    var segment: String,
    var statusLabel: String,
)

fun toBatchedScorecardBundleView(item: BatchedScorecardBundle): BatchedScorecardBundleView {
    return BatchedScorecardBundleView(
        code = truncate(item.code, 40),
        title = item.title,
        margin = "%.2f".format(item.margin),
        windowDays = item.windowDays.toString(),
        segment = item.segment,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toBatchedScorecardBundleViews(items: MutableList<BatchedScorecardBundle>): MutableList<BatchedScorecardBundleView> {
    val views = mutableListOf<BatchedScorecardBundleView>()
    for (item in items) {
        views.add(toBatchedScorecardBundleView(item))
    }
    return views
}

fun sumBatchedScorecardBundleView(view: BatchedScorecardBundleView): Double {
    val parsedMargin = view.margin.toDouble()
    val parsedWindowDays = view.windowDays.toInt()
    return parsedMargin + parsedWindowDays.toDouble()
}
