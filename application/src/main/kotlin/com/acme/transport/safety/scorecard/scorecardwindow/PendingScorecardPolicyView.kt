package com.acme.transport.safety.scorecard.scorecardwindow

import com.acme.transport.kit.text.truncate

/** How screens and exports show a pending scorecard policy. */
data class PendingScorecardPolicyView(
    var code: String,
    var title: String,
    var sourceText: String,
    var sequence: String,
    var pinned: String,
    var durationMinutes: String,
    var limitText: String,
    var channel: String,
    var unit: String,
    var segmentText: String,
    var statusLabel: String,
)

fun toPendingScorecardPolicyView(item: PendingScorecardPolicy): PendingScorecardPolicyView {
    return PendingScorecardPolicyView(
        code = item.code,
        title = truncate(item.title, 80),
        sourceText = item.source,
        sequence = item.sequence.toString(),
        pinned = "${item.pinned}",
        durationMinutes = item.durationMinutes.toString(),
        limitText = item.limit.toString(),
        channel = item.channel,
        unit = truncate(item.unit, 80),
        segmentText = truncate(item.segment, 64),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPendingScorecardPolicyViews(items: MutableList<PendingScorecardPolicy>): MutableList<PendingScorecardPolicyView> {
    val views = mutableListOf<PendingScorecardPolicyView>()
    for (item in items) {
        views.add(toPendingScorecardPolicyView(item))
    }
    return views
}

fun sumPendingScorecardPolicyView(view: PendingScorecardPolicyView): Double {
    val parsedSequence = view.sequence.toInt()
    val parsedDurationMinutes = view.durationMinutes.toInt()
    val parsedLimit = view.limitText.toInt()
    return parsedSequence.toDouble() + parsedDurationMinutes.toDouble() + parsedLimit.toDouble()
}
