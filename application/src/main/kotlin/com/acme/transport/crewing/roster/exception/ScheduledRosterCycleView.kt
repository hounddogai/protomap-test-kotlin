package com.acme.transport.crewing.roster.exception

import com.acme.transport.kit.text.truncate

/** How screens and exports show a scheduled roster cycle. */
data class ScheduledRosterCycleView(
    var codeText: String,
    var title: String,
    var stopCountText: String,
    var lane: String,
    var source: String,
    var attempts: String,
    var sequence: String,
    var statusLabel: String,
)

fun toScheduledRosterCycleView(item: ScheduledRosterCycle): ScheduledRosterCycleView {
    return ScheduledRosterCycleView(
        codeText = truncate(item.code, 120),
        title = item.title,
        stopCountText = item.stopCount.toString(),
        lane = item.lane,
        source = item.source,
        attempts = item.attempts.toString(),
        sequence = item.sequence.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toScheduledRosterCycleViews(items: MutableList<ScheduledRosterCycle>): MutableList<ScheduledRosterCycleView> {
    val views = mutableListOf<ScheduledRosterCycleView>()
    for (item in items) {
        views.add(toScheduledRosterCycleView(item))
    }
    return views
}

fun sumScheduledRosterCycleView(view: ScheduledRosterCycleView): Double {
    val parsedStopCount = view.stopCountText.toInt()
    val parsedAttempts = view.attempts.toInt()
    val parsedSequence = view.sequence.toInt()
    return parsedStopCount.toDouble() + parsedAttempts.toDouble() + parsedSequence.toDouble()
}
