package com.acme.transport.lastmile.parcel.policy

import com.acme.transport.kit.text.truncate

/** How screens and exports show a parcel snapshot. */
data class ParcelSnapshotView(
    var code: String,
    var labelText: String,
    var name: String,
    var capacity: String,
    var reviewer: String,
    var odometerText: String,
    var vehicleId: String,
    var trailerId: String,
    var statusLabel: String,
)

fun toParcelSnapshotView(item: ParcelSnapshot): ParcelSnapshotView {
    return ParcelSnapshotView(
        code = item.code,
        labelText = item.label,
        name = item.name,
        capacity = item.capacity.toString(),
        reviewer = item.reviewer,
        odometerText = item.odometer.toString(),
        vehicleId = item.vehicleId,
        trailerId = truncate(item.trailerId, 40),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toParcelSnapshotViews(items: MutableList<ParcelSnapshot>): MutableList<ParcelSnapshotView> {
    val views = mutableListOf<ParcelSnapshotView>()
    for (item in items) {
        views.add(toParcelSnapshotView(item))
    }
    return views
}

fun sumParcelSnapshotView(view: ParcelSnapshotView): Double {
    val parsedCapacity = view.capacity.toInt()
    val parsedOdometer = view.odometerText.toInt()
    return parsedCapacity.toDouble() + parsedOdometer.toDouble()
}
