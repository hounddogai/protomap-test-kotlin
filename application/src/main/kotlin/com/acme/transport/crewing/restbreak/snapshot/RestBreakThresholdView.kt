package com.acme.transport.crewing.restbreak.snapshot

import com.acme.transport.kit.text.truncate

/** How screens and exports show a rest break threshold. */
data class RestBreakThresholdView(
    var code: String,
    var name: String,
    var ownerIdText: String,
    var scoreText: String,
    var threshold: String,
    var statusLabel: String,
)

fun toRestBreakThresholdView(item: RestBreakThreshold): RestBreakThresholdView {
    return RestBreakThresholdView(
        code = truncate(item.code, 80),
        name = truncate(item.name, 40),
        ownerIdText = item.ownerId,
        scoreText = "%.2f".format(item.score),
        threshold = "%.2f".format(item.threshold),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRestBreakThresholdViews(items: MutableList<RestBreakThreshold>): MutableList<RestBreakThresholdView> {
    val views = mutableListOf<RestBreakThresholdView>()
    for (item in items) {
        views.add(toRestBreakThresholdView(item))
    }
    return views
}

fun sumRestBreakThresholdView(view: RestBreakThresholdView): Double {
    val parsedScore = view.scoreText.toDouble()
    val parsedThreshold = view.threshold.toDouble()
    return parsedScore + parsedThreshold
}
