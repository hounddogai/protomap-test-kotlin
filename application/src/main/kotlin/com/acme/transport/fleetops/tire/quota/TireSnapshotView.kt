package com.acme.transport.fleetops.tire.quota

import com.acme.transport.kit.text.truncate

/** How screens and exports show a tire snapshot. */
data class TireSnapshotView(
    var code: String,
    var label: String,
    var stopCount: String,
    var maximum: String,
    var pinned: String,
    var active: String,
    var phoneText: String,
    var approved: String,
    var statusLabel: String,
)

fun toTireSnapshotView(item: TireSnapshot): TireSnapshotView {
    return TireSnapshotView(
        code = truncate(item.code, 64),
        label = item.label,
        stopCount = item.stopCount.toString(),
        maximum = "%.2f".format(item.maximum),
        pinned = "${item.pinned}",
        active = "${item.active}",
        phoneText = truncate(item.phone, 40),
        approved = "${item.approved}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTireSnapshotViews(items: MutableList<TireSnapshot>): MutableList<TireSnapshotView> {
    val views = mutableListOf<TireSnapshotView>()
    for (item in items) {
        views.add(toTireSnapshotView(item))
    }
    return views
}

fun sumTireSnapshotView(view: TireSnapshotView): Double {
    val parsedStopCount = view.stopCount.toInt()
    val parsedMaximum = view.maximum.toDouble()
    return parsedStopCount.toDouble() + parsedMaximum
}
