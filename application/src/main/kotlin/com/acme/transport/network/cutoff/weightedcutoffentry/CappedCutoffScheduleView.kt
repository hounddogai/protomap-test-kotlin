package com.acme.transport.network.cutoff.weightedcutoffentry

import com.acme.transport.kit.text.truncate

/** How screens and exports show a capped cutoff schedule. */
data class CappedCutoffScheduleView(
    var code: String,
    var name: String,
    var updatedAt: String,
    var active: String,
    var shiftCodeText: String,
    var minimumText: String,
    var trailerId: String,
    var carrier: String,
    var vehicleIdText: String,
    var statusLabel: String,
)

fun toCappedCutoffScheduleView(item: CappedCutoffSchedule): CappedCutoffScheduleView {
    return CappedCutoffScheduleView(
        code = item.code,
        name = item.name,
        updatedAt = item.updatedAt.toString(),
        active = "${item.active}",
        shiftCodeText = item.shiftCode,
        minimumText = "%.2f".format(item.minimum),
        trailerId = truncate(item.trailerId, 64),
        carrier = truncate(item.carrier, 120),
        vehicleIdText = item.vehicleId,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCappedCutoffScheduleViews(items: MutableList<CappedCutoffSchedule>): MutableList<CappedCutoffScheduleView> {
    val views = mutableListOf<CappedCutoffScheduleView>()
    for (item in items) {
        views.add(toCappedCutoffScheduleView(item))
    }
    return views
}

fun sumCappedCutoffScheduleView(view: CappedCutoffScheduleView): Double {
    val parsedUpdatedAt = view.updatedAt.toInt()
    val parsedMinimum = view.minimumText.toDouble()
    return parsedUpdatedAt.toDouble() + parsedMinimum
}
