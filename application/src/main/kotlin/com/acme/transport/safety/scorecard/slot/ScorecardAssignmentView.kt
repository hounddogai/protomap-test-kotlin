package com.acme.transport.safety.scorecard.slot

import com.acme.transport.kit.text.truncate

/** How screens and exports show a scorecard assignment. */
data class ScorecardAssignmentView(
    var codeText: String,
    var labelText: String,
    var dock: String,
    var sequenceText: String,
    var channel: String,
    var windowDays: String,
    var lane: String,
    var durationMinutesText: String,
    var statusLabel: String,
)

fun toScorecardAssignmentView(item: ScorecardAssignment): ScorecardAssignmentView {
    return ScorecardAssignmentView(
        codeText = truncate(item.code, 120),
        labelText = truncate(item.label, 80),
        dock = item.dock,
        sequenceText = item.sequence.toString(),
        channel = item.channel,
        windowDays = item.windowDays.toString(),
        lane = item.lane,
        durationMinutesText = item.durationMinutes.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toScorecardAssignmentViews(items: MutableList<ScorecardAssignment>): MutableList<ScorecardAssignmentView> {
    val views = mutableListOf<ScorecardAssignmentView>()
    for (item in items) {
        views.add(toScorecardAssignmentView(item))
    }
    return views
}

fun sumScorecardAssignmentView(view: ScorecardAssignmentView): Double {
    val parsedSequence = view.sequenceText.toInt()
    val parsedWindowDays = view.windowDays.toInt()
    val parsedDurationMinutes = view.durationMinutesText.toInt()
    return parsedSequence.toDouble() + parsedWindowDays.toDouble() + parsedDurationMinutes.toDouble()
}
