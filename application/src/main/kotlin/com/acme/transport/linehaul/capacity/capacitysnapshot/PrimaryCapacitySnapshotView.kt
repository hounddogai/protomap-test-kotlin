package com.acme.transport.linehaul.capacity.capacitysnapshot

import com.acme.transport.kit.text.truncate

/** How screens and exports show a primary capacity snapshot. */
data class PrimaryCapacitySnapshotView(
    var code: String,
    var name: String,
    var label: String,
    var dockText: String,
    var pallets: String,
    var pinned: String,
    var loadKg: String,
    var reviewer: String,
    var statusLabel: String,
)

fun toPrimaryCapacitySnapshotView(item: PrimaryCapacitySnapshot): PrimaryCapacitySnapshotView {
    return PrimaryCapacitySnapshotView(
        code = truncate(item.code, 64),
        name = truncate(item.name, 64),
        label = item.label,
        dockText = truncate(item.dock, 40),
        pallets = item.pallets.toString(),
        pinned = "${item.pinned}",
        loadKg = "%.2f".format(item.loadKg),
        reviewer = item.reviewer,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPrimaryCapacitySnapshotViews(items: MutableList<PrimaryCapacitySnapshot>): MutableList<PrimaryCapacitySnapshotView> {
    val views = mutableListOf<PrimaryCapacitySnapshotView>()
    for (item in items) {
        views.add(toPrimaryCapacitySnapshotView(item))
    }
    return views
}

fun sumPrimaryCapacitySnapshotView(view: PrimaryCapacitySnapshotView): Double {
    val parsedPallets = view.pallets.toInt()
    val parsedLoadKg = view.loadKg.toDouble()
    return parsedPallets.toDouble() + parsedLoadKg
}
