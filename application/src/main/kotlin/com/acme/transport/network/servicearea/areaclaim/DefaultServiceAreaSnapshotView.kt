package com.acme.transport.network.servicearea.areaclaim

import com.acme.transport.kit.text.truncate

/** How screens and exports show a default service area snapshot. */
data class DefaultServiceAreaSnapshotView(
    var code: String,
    var nameText: String,
    var sequence: String,
    var floorValue: String,
    var distanceKm: String,
    var statusLabel: String,
)

fun toDefaultServiceAreaSnapshotView(item: DefaultServiceAreaSnapshot): DefaultServiceAreaSnapshotView {
    return DefaultServiceAreaSnapshotView(
        code = item.code,
        nameText = truncate(item.name, 120),
        sequence = item.sequence.toString(),
        floorValue = "%.2f".format(item.floorValue),
        distanceKm = "%.2f".format(item.distanceKm),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDefaultServiceAreaSnapshotViews(items: MutableList<DefaultServiceAreaSnapshot>): MutableList<DefaultServiceAreaSnapshotView> {
    val views = mutableListOf<DefaultServiceAreaSnapshotView>()
    for (item in items) {
        views.add(toDefaultServiceAreaSnapshotView(item))
    }
    return views
}

fun sumDefaultServiceAreaSnapshotView(view: DefaultServiceAreaSnapshotView): Double {
    val parsedSequence = view.sequence.toInt()
    val parsedFloorValue = view.floorValue.toDouble()
    val parsedDistanceKm = view.distanceKm.toDouble()
    return parsedSequence.toDouble() + parsedFloorValue + parsedDistanceKm
}
