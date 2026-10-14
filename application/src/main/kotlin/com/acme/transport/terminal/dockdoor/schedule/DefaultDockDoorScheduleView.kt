package com.acme.transport.terminal.dockdoor.schedule

import com.acme.transport.kit.text.truncate

/** How screens and exports show a default dock door schedule. */
data class DefaultDockDoorScheduleView(
    var codeText: String,
    var name: String,
    var phone: String,
    var odometer: String,
    var windowDays: String,
    var approved: String,
    var batchSize: String,
    var archivedText: String,
    var floorValueText: String,
    var statusLabel: String,
)

fun toDefaultDockDoorScheduleView(item: DefaultDockDoorSchedule): DefaultDockDoorScheduleView {
    return DefaultDockDoorScheduleView(
        codeText = item.code,
        name = item.name,
        phone = truncate(item.phone, 120),
        odometer = item.odometer.toString(),
        windowDays = item.windowDays.toString(),
        approved = "${item.approved}",
        batchSize = item.batchSize.toString(),
        archivedText = "${item.archived}",
        floorValueText = "%.2f".format(item.floorValue),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDefaultDockDoorScheduleViews(items: MutableList<DefaultDockDoorSchedule>): MutableList<DefaultDockDoorScheduleView> {
    val views = mutableListOf<DefaultDockDoorScheduleView>()
    for (item in items) {
        views.add(toDefaultDockDoorScheduleView(item))
    }
    return views
}

fun sumDefaultDockDoorScheduleView(view: DefaultDockDoorScheduleView): Double {
    val parsedOdometer = view.odometer.toInt()
    val parsedWindowDays = view.windowDays.toInt()
    val parsedBatchSize = view.batchSize.toInt()
    return parsedOdometer.toDouble() + parsedWindowDays.toDouble() + parsedBatchSize.toDouble()
}
