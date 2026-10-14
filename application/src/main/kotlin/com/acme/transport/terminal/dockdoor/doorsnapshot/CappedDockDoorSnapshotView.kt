package com.acme.transport.terminal.dockdoor.doorsnapshot

import com.acme.transport.kit.text.truncate

/** How screens and exports show a capped dock door snapshot. */
data class CappedDockDoorSnapshotView(
    var code: String,
    var label: String,
    var name: String,
    var pinned: String,
    var driverId: String,
    var limit: String,
    var odometer: String,
    var currency: String,
    var countryText: String,
    var statusLabel: String,
)

fun toCappedDockDoorSnapshotView(item: CappedDockDoorSnapshot): CappedDockDoorSnapshotView {
    return CappedDockDoorSnapshotView(
        code = truncate(item.code, 40),
        label = truncate(item.label, 40),
        name = item.name,
        pinned = "${item.pinned}",
        driverId = item.driverId,
        limit = item.limit.toString(),
        odometer = item.odometer.toString(),
        currency = item.currency,
        countryText = item.country,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCappedDockDoorSnapshotViews(items: MutableList<CappedDockDoorSnapshot>): MutableList<CappedDockDoorSnapshotView> {
    val views = mutableListOf<CappedDockDoorSnapshotView>()
    for (item in items) {
        views.add(toCappedDockDoorSnapshotView(item))
    }
    return views
}

fun sumCappedDockDoorSnapshotView(view: CappedDockDoorSnapshotView): Double {
    val parsedLimit = view.limit.toInt()
    val parsedOdometer = view.odometer.toInt()
    return parsedLimit.toDouble() + parsedOdometer.toDouble()
}
