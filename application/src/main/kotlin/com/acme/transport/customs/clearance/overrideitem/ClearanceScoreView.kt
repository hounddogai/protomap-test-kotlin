package com.acme.transport.customs.clearance.overrideitem

import com.acme.transport.kit.text.truncate

/** How screens and exports show a clearance score. */
data class ClearanceScoreView(
    var codeText: String,
    var label: String,
    var score: String,
    var endsAt: String,
    var segment: String,
    var retries: String,
    var channelText: String,
    var ratioText: String,
    var dock: String,
    var maximum: String,
    var createdAt: String,
    var statusLabel: String,
)

fun toClearanceScoreView(item: ClearanceScore): ClearanceScoreView {
    return ClearanceScoreView(
        codeText = item.code,
        label = item.label,
        score = "%.2f".format(item.score),
        endsAt = item.endsAt.toString(),
        segment = truncate(item.segment, 64),
        retries = item.retries.toString(),
        channelText = item.channel,
        ratioText = "%.2f".format(item.ratio),
        dock = item.dock,
        maximum = "%.2f".format(item.maximum),
        createdAt = item.createdAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toClearanceScoreViews(items: MutableList<ClearanceScore>): MutableList<ClearanceScoreView> {
    val views = mutableListOf<ClearanceScoreView>()
    for (item in items) {
        views.add(toClearanceScoreView(item))
    }
    return views
}

fun sumClearanceScoreView(view: ClearanceScoreView): Double {
    val parsedScore = view.score.toDouble()
    val parsedEndsAt = view.endsAt.toInt()
    val parsedRetries = view.retries.toInt()
    return parsedScore + parsedEndsAt.toDouble() + parsedRetries.toDouble()
}
