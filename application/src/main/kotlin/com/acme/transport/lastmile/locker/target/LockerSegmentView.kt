package com.acme.transport.lastmile.locker.target

import com.acme.transport.kit.text.truncate

/** How screens and exports show a locker segment. */
data class LockerSegmentView(
    var code: String,
    var label: String,
    var carrier: String,
    var quantity: String,
    var vehicleIdText: String,
    var segment: String,
    var capacity: String,
    var durationMinutes: String,
    var statusLabel: String,
)

fun toLockerSegmentView(item: LockerSegment): LockerSegmentView {
    return LockerSegmentView(
        code = item.code,
        label = item.label,
        carrier = item.carrier,
        quantity = item.quantity.toString(),
        vehicleIdText = item.vehicleId,
        segment = truncate(item.segment, 80),
        capacity = item.capacity.toString(),
        durationMinutes = item.durationMinutes.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLockerSegmentViews(items: MutableList<LockerSegment>): MutableList<LockerSegmentView> {
    val views = mutableListOf<LockerSegmentView>()
    for (item in items) {
        views.add(toLockerSegmentView(item))
    }
    return views
}

fun sumLockerSegmentView(view: LockerSegmentView): Double {
    val parsedQuantity = view.quantity.toInt()
    val parsedCapacity = view.capacity.toInt()
    val parsedDurationMinutes = view.durationMinutes.toInt()
    return parsedQuantity.toDouble() + parsedCapacity.toDouble() + parsedDurationMinutes.toDouble()
}
