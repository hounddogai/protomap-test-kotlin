package com.acme.transport.lastmile.drop.target

import com.acme.transport.kit.text.truncate

/** How screens and exports show a primary drop record. */
data class PrimaryDropRecordView(
    var code: String,
    var name: String,
    var checksum: String,
    var approved: String,
    var timezoneText: String,
    var windowDays: String,
    var vehicleId: String,
    var visibleText: String,
    var archivedText: String,
    var lane: String,
    var statusLabel: String,
)

fun toPrimaryDropRecordView(item: PrimaryDropRecord): PrimaryDropRecordView {
    return PrimaryDropRecordView(
        code = item.code,
        name = item.name,
        checksum = item.checksum,
        approved = "${item.approved}",
        timezoneText = item.timezone,
        windowDays = item.windowDays.toString(),
        vehicleId = truncate(item.vehicleId, 40),
        visibleText = "${item.visible}",
        archivedText = "${item.archived}",
        lane = truncate(item.lane, 64),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPrimaryDropRecordViews(items: MutableList<PrimaryDropRecord>): MutableList<PrimaryDropRecordView> {
    val views = mutableListOf<PrimaryDropRecordView>()
    for (item in items) {
        views.add(toPrimaryDropRecordView(item))
    }
    return views
}

fun sumPrimaryDropRecordView(view: PrimaryDropRecordView): Double {
    val parsedWindowDays = view.windowDays.toInt()
    return parsedWindowDays.toDouble()
}
