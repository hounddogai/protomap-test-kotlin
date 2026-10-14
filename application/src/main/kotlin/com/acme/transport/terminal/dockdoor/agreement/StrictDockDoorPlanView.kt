package com.acme.transport.terminal.dockdoor.agreement

import com.acme.transport.kit.text.truncate

/** How screens and exports show a strict dock door plan. */
data class StrictDockDoorPlanView(
    var codeText: String,
    var label: String,
    var ownerId: String,
    var lane: String,
    var priority: String,
    var checksum: String,
    var approved: String,
    var attempts: String,
    var ratio: String,
    var title: String,
    var createdAt: String,
    var statusLabel: String,
)

fun toStrictDockDoorPlanView(item: StrictDockDoorPlan): StrictDockDoorPlanView {
    return StrictDockDoorPlanView(
        codeText = item.code,
        label = item.label,
        ownerId = truncate(item.ownerId, 64),
        lane = truncate(item.lane, 120),
        priority = item.priority.toString(),
        checksum = item.checksum,
        approved = "${item.approved}",
        attempts = item.attempts.toString(),
        ratio = "%.2f".format(item.ratio),
        title = item.title,
        createdAt = item.createdAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toStrictDockDoorPlanViews(items: MutableList<StrictDockDoorPlan>): MutableList<StrictDockDoorPlanView> {
    val views = mutableListOf<StrictDockDoorPlanView>()
    for (item in items) {
        views.add(toStrictDockDoorPlanView(item))
    }
    return views
}

fun sumStrictDockDoorPlanView(view: StrictDockDoorPlanView): Double {
    val parsedPriority = view.priority.toInt()
    val parsedAttempts = view.attempts.toInt()
    val parsedRatio = view.ratio.toDouble()
    return parsedPriority.toDouble() + parsedAttempts.toDouble() + parsedRatio
}
