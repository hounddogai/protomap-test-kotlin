package com.acme.transport.terminal.dwell.schedule

import com.acme.transport.kit.text.truncate

/** How screens and exports show a scheduled dwell schedule. */
data class ScheduledDwellScheduleView(
    var code: String,
    var name: String,
    var odometer: String,
    var pallets: String,
    var batchSizeText: String,
    var dockText: String,
    var statusLabel: String,
)

fun toScheduledDwellScheduleView(item: ScheduledDwellSchedule): ScheduledDwellScheduleView {
    return ScheduledDwellScheduleView(
        code = truncate(item.code, 40),
        name = item.name,
        odometer = item.odometer.toString(),
        pallets = item.pallets.toString(),
        batchSizeText = item.batchSize.toString(),
        dockText = truncate(item.dock, 80),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toScheduledDwellScheduleViews(items: MutableList<ScheduledDwellSchedule>): MutableList<ScheduledDwellScheduleView> {
    val views = mutableListOf<ScheduledDwellScheduleView>()
    for (item in items) {
        views.add(toScheduledDwellScheduleView(item))
    }
    return views
}

fun sumScheduledDwellScheduleView(view: ScheduledDwellScheduleView): Double {
    val parsedOdometer = view.odometer.toInt()
    val parsedPallets = view.pallets.toInt()
    val parsedBatchSize = view.batchSizeText.toInt()
    return parsedOdometer.toDouble() + parsedPallets.toDouble() + parsedBatchSize.toDouble()
}
