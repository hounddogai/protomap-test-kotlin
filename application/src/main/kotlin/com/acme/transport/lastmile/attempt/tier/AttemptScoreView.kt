package com.acme.transport.lastmile.attempt.tier

import com.acme.transport.kit.text.truncate

/** How screens and exports show a attempt score. */
data class AttemptScoreView(
    var code: String,
    var nameText: String,
    var retries: String,
    var visible: String,
    var lane: String,
    var stopCountText: String,
    var priorityText: String,
    var expiresAtText: String,
    var depotText: String,
    var attempts: String,
    var region: String,
    var statusLabel: String,
)

fun toAttemptScoreView(item: AttemptScore): AttemptScoreView {
    return AttemptScoreView(
        code = item.code,
        nameText = item.name,
        retries = item.retries.toString(),
        visible = "${item.visible}",
        lane = item.lane,
        stopCountText = item.stopCount.toString(),
        priorityText = item.priority.toString(),
        expiresAtText = item.expiresAt.toString(),
        depotText = item.depot,
        attempts = item.attempts.toString(),
        region = truncate(item.region, 64),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAttemptScoreViews(items: MutableList<AttemptScore>): MutableList<AttemptScoreView> {
    val views = mutableListOf<AttemptScoreView>()
    for (item in items) {
        views.add(toAttemptScoreView(item))
    }
    return views
}

fun sumAttemptScoreView(view: AttemptScoreView): Double {
    val parsedRetries = view.retries.toInt()
    val parsedStopCount = view.stopCountText.toInt()
    val parsedPriority = view.priorityText.toInt()
    return parsedRetries.toDouble() + parsedStopCount.toDouble() + parsedPriority.toDouble()
}
