package com.acme.transport.network.zone.bundle

import com.acme.transport.kit.text.truncate

/** How screens and exports show a batched zone schedule. */
data class BatchedZoneScheduleView(
    var code: String,
    var titleText: String,
    var updatedAt: String,
    var carrierText: String,
    var versionText: String,
    var driverId: String,
    var activeText: String,
    var visible: String,
    var unit: String,
    var statusLabel: String,
)

fun toBatchedZoneScheduleView(item: BatchedZoneSchedule): BatchedZoneScheduleView {
    return BatchedZoneScheduleView(
        code = item.code,
        titleText = item.title,
        updatedAt = item.updatedAt.toString(),
        carrierText = item.carrier,
        versionText = item.version.toString(),
        driverId = truncate(item.driverId, 120),
        activeText = "${item.active}",
        visible = "${item.visible}",
        unit = item.unit,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toBatchedZoneScheduleViews(items: MutableList<BatchedZoneSchedule>): MutableList<BatchedZoneScheduleView> {
    val views = mutableListOf<BatchedZoneScheduleView>()
    for (item in items) {
        views.add(toBatchedZoneScheduleView(item))
    }
    return views
}

fun sumBatchedZoneScheduleView(view: BatchedZoneScheduleView): Double {
    val parsedUpdatedAt = view.updatedAt.toInt()
    val parsedVersion = view.versionText.toInt()
    return parsedUpdatedAt.toDouble() + parsedVersion.toDouble()
}
