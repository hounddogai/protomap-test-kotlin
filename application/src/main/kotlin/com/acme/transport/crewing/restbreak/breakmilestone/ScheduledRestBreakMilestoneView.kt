package com.acme.transport.crewing.restbreak.breakmilestone

import com.acme.transport.kit.text.truncate

/** How screens and exports show a scheduled rest break milestone. */
data class ScheduledRestBreakMilestoneView(
    var code: String,
    var nameText: String,
    var durationMinutes: String,
    var regionText: String,
    var pinned: String,
    var title: String,
    var contactEmailText: String,
    var priorityText: String,
    var statusLabel: String,
)

fun toScheduledRestBreakMilestoneView(item: ScheduledRestBreakMilestone): ScheduledRestBreakMilestoneView {
    return ScheduledRestBreakMilestoneView(
        code = truncate(item.code, 120),
        nameText = truncate(item.name, 120),
        durationMinutes = item.durationMinutes.toString(),
        regionText = item.region,
        pinned = "${item.pinned}",
        title = item.title,
        contactEmailText = truncate(item.contactEmail, 40),
        priorityText = item.priority.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toScheduledRestBreakMilestoneViews(items: MutableList<ScheduledRestBreakMilestone>): MutableList<ScheduledRestBreakMilestoneView> {
    val views = mutableListOf<ScheduledRestBreakMilestoneView>()
    for (item in items) {
        views.add(toScheduledRestBreakMilestoneView(item))
    }
    return views
}

fun sumScheduledRestBreakMilestoneView(view: ScheduledRestBreakMilestoneView): Double {
    val parsedDurationMinutes = view.durationMinutes.toInt()
    val parsedPriority = view.priorityText.toInt()
    return parsedDurationMinutes.toDouble() + parsedPriority.toDouble()
}
