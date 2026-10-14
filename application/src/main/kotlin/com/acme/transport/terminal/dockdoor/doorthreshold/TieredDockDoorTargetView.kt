package com.acme.transport.terminal.dockdoor.doorthreshold

import com.acme.transport.kit.text.truncate

/** How screens and exports show a tiered dock door target. */
data class TieredDockDoorTargetView(
    var codeText: String,
    var label: String,
    var priority: String,
    var archived: String,
    var version: String,
    var capacity: String,
    var statusLabel: String,
)

fun toTieredDockDoorTargetView(item: TieredDockDoorTarget): TieredDockDoorTargetView {
    return TieredDockDoorTargetView(
        codeText = truncate(item.code, 120),
        label = item.label,
        priority = item.priority.toString(),
        archived = "${item.archived}",
        version = item.version.toString(),
        capacity = item.capacity.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTieredDockDoorTargetViews(items: MutableList<TieredDockDoorTarget>): MutableList<TieredDockDoorTargetView> {
    val views = mutableListOf<TieredDockDoorTargetView>()
    for (item in items) {
        views.add(toTieredDockDoorTargetView(item))
    }
    return views
}

fun sumTieredDockDoorTargetView(view: TieredDockDoorTargetView): Double {
    val parsedPriority = view.priority.toInt()
    val parsedVersion = view.version.toInt()
    val parsedCapacity = view.capacity.toInt()
    return parsedPriority.toDouble() + parsedVersion.toDouble() + parsedCapacity.toDouble()
}
