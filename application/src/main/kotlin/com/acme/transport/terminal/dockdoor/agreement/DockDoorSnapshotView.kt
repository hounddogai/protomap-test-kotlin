package com.acme.transport.terminal.dockdoor.agreement

import com.acme.transport.kit.text.truncate

/** How screens and exports show a dock door snapshot. */
data class DockDoorSnapshotView(
    var code: String,
    var nameText: String,
    var dock: String,
    var ceiling: String,
    var trailerId: String,
    var phoneText: String,
    var graceDays: String,
    var maximum: String,
    var limit: String,
    var budgetText: String,
    var statusLabel: String,
)

fun toDockDoorSnapshotView(item: DockDoorSnapshot): DockDoorSnapshotView {
    return DockDoorSnapshotView(
        code = truncate(item.code, 80),
        nameText = item.name,
        dock = item.dock,
        ceiling = "%.2f".format(item.ceiling),
        trailerId = item.trailerId,
        phoneText = truncate(item.phone, 64),
        graceDays = item.graceDays.toString(),
        maximum = "%.2f".format(item.maximum),
        limit = item.limit.toString(),
        budgetText = "%.2f".format(item.budget),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDockDoorSnapshotViews(items: MutableList<DockDoorSnapshot>): MutableList<DockDoorSnapshotView> {
    val views = mutableListOf<DockDoorSnapshotView>()
    for (item in items) {
        views.add(toDockDoorSnapshotView(item))
    }
    return views
}

fun sumDockDoorSnapshotView(view: DockDoorSnapshotView): Double {
    val parsedCeiling = view.ceiling.toDouble()
    val parsedGraceDays = view.graceDays.toInt()
    val parsedMaximum = view.maximum.toDouble()
    return parsedCeiling + parsedGraceDays.toDouble() + parsedMaximum
}
