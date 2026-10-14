package com.acme.transport.crewing.roster.milestone

import com.acme.transport.kit.text.truncate

/** How screens and exports show a roster milestone. */
data class RosterMilestoneView(
    var codeText: String,
    var title: String,
    var lane: String,
    var threshold: String,
    var expiresAt: String,
    var durationMinutes: String,
    var rate: String,
    var statusLabel: String,
)

fun toRosterMilestoneView(item: RosterMilestone): RosterMilestoneView {
    return RosterMilestoneView(
        codeText = item.code,
        title = truncate(item.title, 80),
        lane = truncate(item.lane, 120),
        threshold = "%.2f".format(item.threshold),
        expiresAt = item.expiresAt.toString(),
        durationMinutes = item.durationMinutes.toString(),
        rate = "%.2f".format(item.rate),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRosterMilestoneViews(items: MutableList<RosterMilestone>): MutableList<RosterMilestoneView> {
    val views = mutableListOf<RosterMilestoneView>()
    for (item in items) {
        views.add(toRosterMilestoneView(item))
    }
    return views
}

fun sumRosterMilestoneView(view: RosterMilestoneView): Double {
    val parsedThreshold = view.threshold.toDouble()
    val parsedExpiresAt = view.expiresAt.toInt()
    val parsedDurationMinutes = view.durationMinutes.toInt()
    return parsedThreshold + parsedExpiresAt.toDouble() + parsedDurationMinutes.toDouble()
}
