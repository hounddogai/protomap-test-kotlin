package com.acme.transport.network.zone.draft

import com.acme.transport.kit.text.truncate

/** How screens and exports show a rolling zone checkpoint. */
data class RollingZoneCheckpointView(
    var codeText: String,
    var title: String,
    var durationMinutes: String,
    var visible: String,
    var approved: String,
    var updatedAtText: String,
    var version: String,
    var statusLabel: String,
)

fun toRollingZoneCheckpointView(item: RollingZoneCheckpoint): RollingZoneCheckpointView {
    return RollingZoneCheckpointView(
        codeText = truncate(item.code, 40),
        title = truncate(item.title, 120),
        durationMinutes = item.durationMinutes.toString(),
        visible = "${item.visible}",
        approved = "${item.approved}",
        updatedAtText = item.updatedAt.toString(),
        version = item.version.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRollingZoneCheckpointViews(items: MutableList<RollingZoneCheckpoint>): MutableList<RollingZoneCheckpointView> {
    val views = mutableListOf<RollingZoneCheckpointView>()
    for (item in items) {
        views.add(toRollingZoneCheckpointView(item))
    }
    return views
}

fun sumRollingZoneCheckpointView(view: RollingZoneCheckpointView): Double {
    val parsedDurationMinutes = view.durationMinutes.toInt()
    val parsedUpdatedAt = view.updatedAtText.toInt()
    val parsedVersion = view.version.toInt()
    return parsedDurationMinutes.toDouble() + parsedUpdatedAt.toDouble() + parsedVersion.toDouble()
}
