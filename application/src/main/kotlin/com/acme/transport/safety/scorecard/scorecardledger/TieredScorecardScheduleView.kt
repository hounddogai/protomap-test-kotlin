package com.acme.transport.safety.scorecard.scorecardledger

import com.acme.transport.kit.text.truncate

/** How screens and exports show a tiered scorecard schedule. */
data class TieredScorecardScheduleView(
    var code: String,
    var title: String,
    var vehicleId: String,
    var description: String,
    var pinned: String,
    var floorValueText: String,
    var checksum: String,
    var archived: String,
    var carrier: String,
    var durationMinutes: String,
    var statusLabel: String,
)

fun toTieredScorecardScheduleView(item: TieredScorecardSchedule): TieredScorecardScheduleView {
    return TieredScorecardScheduleView(
        code = item.code,
        title = item.title,
        vehicleId = item.vehicleId,
        description = truncate(item.description, 64),
        pinned = "${item.pinned}",
        floorValueText = "%.2f".format(item.floorValue),
        checksum = item.checksum,
        archived = "${item.archived}",
        carrier = item.carrier,
        durationMinutes = item.durationMinutes.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTieredScorecardScheduleViews(items: MutableList<TieredScorecardSchedule>): MutableList<TieredScorecardScheduleView> {
    val views = mutableListOf<TieredScorecardScheduleView>()
    for (item in items) {
        views.add(toTieredScorecardScheduleView(item))
    }
    return views
}

fun sumTieredScorecardScheduleView(view: TieredScorecardScheduleView): Double {
    val parsedFloorValue = view.floorValueText.toDouble()
    val parsedDurationMinutes = view.durationMinutes.toInt()
    return parsedFloorValue + parsedDurationMinutes.toDouble()
}
