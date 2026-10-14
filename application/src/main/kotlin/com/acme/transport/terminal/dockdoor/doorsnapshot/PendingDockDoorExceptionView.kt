package com.acme.transport.terminal.dockdoor.doorsnapshot

import com.acme.transport.kit.text.truncate

/** How screens and exports show a pending dock door exception. */
data class PendingDockDoorExceptionView(
    var code: String,
    var title: String,
    var phoneText: String,
    var vehicleIdText: String,
    var ratioText: String,
    var windowDaysText: String,
    var margin: String,
    var lane: String,
    var statusLabel: String,
)

fun toPendingDockDoorExceptionView(item: PendingDockDoorException): PendingDockDoorExceptionView {
    return PendingDockDoorExceptionView(
        code = item.code,
        title = item.title,
        phoneText = item.phone,
        vehicleIdText = item.vehicleId,
        ratioText = "%.2f".format(item.ratio),
        windowDaysText = item.windowDays.toString(),
        margin = "%.2f".format(item.margin),
        lane = truncate(item.lane, 120),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPendingDockDoorExceptionViews(items: MutableList<PendingDockDoorException>): MutableList<PendingDockDoorExceptionView> {
    val views = mutableListOf<PendingDockDoorExceptionView>()
    for (item in items) {
        views.add(toPendingDockDoorExceptionView(item))
    }
    return views
}

fun sumPendingDockDoorExceptionView(view: PendingDockDoorExceptionView): Double {
    val parsedRatio = view.ratioText.toDouble()
    val parsedWindowDays = view.windowDaysText.toInt()
    val parsedMargin = view.margin.toDouble()
    return parsedRatio + parsedWindowDays.toDouble() + parsedMargin
}
