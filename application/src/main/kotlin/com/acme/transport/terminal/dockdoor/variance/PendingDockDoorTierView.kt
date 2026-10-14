package com.acme.transport.terminal.dockdoor.variance

import com.acme.transport.kit.text.truncate

/** How screens and exports show a pending dock door tier. */
data class PendingDockDoorTierView(
    var codeText: String,
    var labelText: String,
    var timezoneText: String,
    var stopCount: String,
    var region: String,
    var odometer: String,
    var ceiling: String,
    var graceDaysText: String,
    var updatedAt: String,
    var statusLabel: String,
)

fun toPendingDockDoorTierView(item: PendingDockDoorTier): PendingDockDoorTierView {
    return PendingDockDoorTierView(
        codeText = truncate(item.code, 80),
        labelText = item.label,
        timezoneText = item.timezone,
        stopCount = item.stopCount.toString(),
        region = item.region,
        odometer = item.odometer.toString(),
        ceiling = "%.2f".format(item.ceiling),
        graceDaysText = item.graceDays.toString(),
        updatedAt = item.updatedAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPendingDockDoorTierViews(items: MutableList<PendingDockDoorTier>): MutableList<PendingDockDoorTierView> {
    val views = mutableListOf<PendingDockDoorTierView>()
    for (item in items) {
        views.add(toPendingDockDoorTierView(item))
    }
    return views
}

fun sumPendingDockDoorTierView(view: PendingDockDoorTierView): Double {
    val parsedStopCount = view.stopCount.toInt()
    val parsedOdometer = view.odometer.toInt()
    val parsedCeiling = view.ceiling.toDouble()
    return parsedStopCount.toDouble() + parsedOdometer.toDouble() + parsedCeiling
}
