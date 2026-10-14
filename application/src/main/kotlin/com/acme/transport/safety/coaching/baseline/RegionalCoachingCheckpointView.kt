package com.acme.transport.safety.coaching.baseline

import com.acme.transport.kit.text.truncate

/** How screens and exports show a regional coaching checkpoint. */
data class RegionalCoachingCheckpointView(
    var code: String,
    var labelText: String,
    var shiftCode: String,
    var active: String,
    var notes: String,
    var odometer: String,
    var fuelLiters: String,
    var statusLabel: String,
)

fun toRegionalCoachingCheckpointView(item: RegionalCoachingCheckpoint): RegionalCoachingCheckpointView {
    return RegionalCoachingCheckpointView(
        code = item.code,
        labelText = truncate(item.label, 64),
        shiftCode = item.shiftCode,
        active = "${item.active}",
        notes = item.notes,
        odometer = item.odometer.toString(),
        fuelLiters = "%.2f".format(item.fuelLiters),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRegionalCoachingCheckpointViews(items: MutableList<RegionalCoachingCheckpoint>): MutableList<RegionalCoachingCheckpointView> {
    val views = mutableListOf<RegionalCoachingCheckpointView>()
    for (item in items) {
        views.add(toRegionalCoachingCheckpointView(item))
    }
    return views
}

fun sumRegionalCoachingCheckpointView(view: RegionalCoachingCheckpointView): Double {
    val parsedOdometer = view.odometer.toInt()
    val parsedFuelLiters = view.fuelLiters.toDouble()
    return parsedOdometer.toDouble() + parsedFuelLiters
}
