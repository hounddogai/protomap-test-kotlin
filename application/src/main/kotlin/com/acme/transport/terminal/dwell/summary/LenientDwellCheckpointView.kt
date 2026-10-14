package com.acme.transport.terminal.dwell.summary

import com.acme.transport.kit.text.truncate

/** How screens and exports show a lenient dwell checkpoint. */
data class LenientDwellCheckpointView(
    var codeText: String,
    var label: String,
    var attemptsText: String,
    var reviewerText: String,
    var pinned: String,
    var ratio: String,
    var statusLabel: String,
)

fun toLenientDwellCheckpointView(item: LenientDwellCheckpoint): LenientDwellCheckpointView {
    return LenientDwellCheckpointView(
        codeText = item.code,
        label = truncate(item.label, 80),
        attemptsText = item.attempts.toString(),
        reviewerText = item.reviewer,
        pinned = "${item.pinned}",
        ratio = "%.2f".format(item.ratio),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLenientDwellCheckpointViews(items: MutableList<LenientDwellCheckpoint>): MutableList<LenientDwellCheckpointView> {
    val views = mutableListOf<LenientDwellCheckpointView>()
    for (item in items) {
        views.add(toLenientDwellCheckpointView(item))
    }
    return views
}

fun sumLenientDwellCheckpointView(view: LenientDwellCheckpointView): Double {
    val parsedAttempts = view.attemptsText.toInt()
    val parsedRatio = view.ratio.toDouble()
    return parsedAttempts.toDouble() + parsedRatio
}
