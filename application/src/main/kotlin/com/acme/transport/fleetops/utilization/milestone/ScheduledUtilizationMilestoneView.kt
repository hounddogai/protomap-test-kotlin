package com.acme.transport.fleetops.utilization.milestone

import com.acme.transport.kit.text.truncate

/** How screens and exports show a scheduled utilization milestone. */
data class ScheduledUtilizationMilestoneView(
    var codeText: String,
    var name: String,
    var category: String,
    var endsAt: String,
    var trailerIdText: String,
    var createdAt: String,
    var attempts: String,
    var cost: String,
    var pinned: String,
    var shiftCodeText: String,
    var statusLabel: String,
)

fun toScheduledUtilizationMilestoneView(item: ScheduledUtilizationMilestone): ScheduledUtilizationMilestoneView {
    return ScheduledUtilizationMilestoneView(
        codeText = truncate(item.code, 80),
        name = truncate(item.name, 120),
        category = item.category,
        endsAt = item.endsAt.toString(),
        trailerIdText = item.trailerId,
        createdAt = item.createdAt.toString(),
        attempts = item.attempts.toString(),
        cost = "%.2f".format(item.cost),
        pinned = "${item.pinned}",
        shiftCodeText = item.shiftCode,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toScheduledUtilizationMilestoneViews(items: MutableList<ScheduledUtilizationMilestone>): MutableList<ScheduledUtilizationMilestoneView> {
    val views = mutableListOf<ScheduledUtilizationMilestoneView>()
    for (item in items) {
        views.add(toScheduledUtilizationMilestoneView(item))
    }
    return views
}

fun sumScheduledUtilizationMilestoneView(view: ScheduledUtilizationMilestoneView): Double {
    val parsedEndsAt = view.endsAt.toInt()
    val parsedCreatedAt = view.createdAt.toInt()
    val parsedAttempts = view.attempts.toInt()
    return parsedEndsAt.toDouble() + parsedCreatedAt.toDouble() + parsedAttempts.toDouble()
}
