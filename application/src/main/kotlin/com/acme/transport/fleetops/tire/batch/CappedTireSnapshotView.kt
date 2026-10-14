package com.acme.transport.fleetops.tire.batch

import com.acme.transport.kit.text.truncate

/** How screens and exports show a capped tire snapshot. */
data class CappedTireSnapshotView(
    var codeText: String,
    var name: String,
    var priority: String,
    var trailerId: String,
    var activeText: String,
    var countryText: String,
    var statusLabel: String,
)

fun toCappedTireSnapshotView(item: CappedTireSnapshot): CappedTireSnapshotView {
    return CappedTireSnapshotView(
        codeText = item.code,
        name = item.name,
        priority = item.priority.toString(),
        trailerId = item.trailerId,
        activeText = "${item.active}",
        countryText = truncate(item.country, 64),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCappedTireSnapshotViews(items: MutableList<CappedTireSnapshot>): MutableList<CappedTireSnapshotView> {
    val views = mutableListOf<CappedTireSnapshotView>()
    for (item in items) {
        views.add(toCappedTireSnapshotView(item))
    }
    return views
}

fun sumCappedTireSnapshotView(view: CappedTireSnapshotView): Double {
    val parsedPriority = view.priority.toInt()
    return parsedPriority.toDouble()
}
