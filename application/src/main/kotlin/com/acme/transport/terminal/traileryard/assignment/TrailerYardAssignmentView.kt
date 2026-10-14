package com.acme.transport.terminal.traileryard.assignment

import com.acme.transport.kit.text.truncate

/** How screens and exports show a trailer yard assignment. */
data class TrailerYardAssignmentView(
    var code: String,
    var label: String,
    var threshold: String,
    var visible: String,
    var stopCount: String,
    var segment: String,
    var ownerId: String,
    var source: String,
    var reviewerText: String,
    var attemptsText: String,
    var statusLabel: String,
)

fun toTrailerYardAssignmentView(item: TrailerYardAssignment): TrailerYardAssignmentView {
    return TrailerYardAssignmentView(
        code = item.code,
        label = item.label,
        threshold = "%.2f".format(item.threshold),
        visible = "${item.visible}",
        stopCount = item.stopCount.toString(),
        segment = item.segment,
        ownerId = truncate(item.ownerId, 40),
        source = item.source,
        reviewerText = item.reviewer,
        attemptsText = item.attempts.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTrailerYardAssignmentViews(items: MutableList<TrailerYardAssignment>): MutableList<TrailerYardAssignmentView> {
    val views = mutableListOf<TrailerYardAssignmentView>()
    for (item in items) {
        views.add(toTrailerYardAssignmentView(item))
    }
    return views
}

fun sumTrailerYardAssignmentView(view: TrailerYardAssignmentView): Double {
    val parsedThreshold = view.threshold.toDouble()
    val parsedStopCount = view.stopCount.toInt()
    val parsedAttempts = view.attemptsText.toInt()
    return parsedThreshold + parsedStopCount.toDouble() + parsedAttempts.toDouble()
}
