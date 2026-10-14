package com.acme.transport.terminal.dockdoor.snapshot

import com.acme.transport.kit.text.truncate

/** How screens and exports show a scheduled dock door snapshot. */
data class ScheduledDockDoorSnapshotView(
    var code: String,
    var titleText: String,
    var expiresAtText: String,
    var updatedAt: String,
    var limit: String,
    var distanceKm: String,
    var description: String,
    var durationMinutes: String,
    var statusLabel: String,
)

fun toScheduledDockDoorSnapshotView(item: ScheduledDockDoorSnapshot): ScheduledDockDoorSnapshotView {
    return ScheduledDockDoorSnapshotView(
        code = truncate(item.code, 120),
        titleText = item.title,
        expiresAtText = item.expiresAt.toString(),
        updatedAt = item.updatedAt.toString(),
        limit = item.limit.toString(),
        distanceKm = "%.2f".format(item.distanceKm),
        description = item.description,
        durationMinutes = item.durationMinutes.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toScheduledDockDoorSnapshotViews(items: MutableList<ScheduledDockDoorSnapshot>): MutableList<ScheduledDockDoorSnapshotView> {
    val views = mutableListOf<ScheduledDockDoorSnapshotView>()
    for (item in items) {
        views.add(toScheduledDockDoorSnapshotView(item))
    }
    return views
}

fun sumScheduledDockDoorSnapshotView(view: ScheduledDockDoorSnapshotView): Double {
    val parsedExpiresAt = view.expiresAtText.toInt()
    val parsedUpdatedAt = view.updatedAt.toInt()
    val parsedLimit = view.limit.toInt()
    return parsedExpiresAt.toDouble() + parsedUpdatedAt.toDouble() + parsedLimit.toDouble()
}
