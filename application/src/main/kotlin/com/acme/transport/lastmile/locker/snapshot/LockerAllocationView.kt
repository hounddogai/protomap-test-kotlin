package com.acme.transport.lastmile.locker.snapshot

import com.acme.transport.kit.text.truncate

/** How screens and exports show a locker allocation. */
data class LockerAllocationView(
    var code: String,
    var name: String,
    var dock: String,
    var createdAt: String,
    var timezone: String,
    var odometer: String,
    var weight: String,
    var priorityText: String,
    var routeCode: String,
    var score: String,
    var statusLabel: String,
)

fun toLockerAllocationView(item: LockerAllocation): LockerAllocationView {
    return LockerAllocationView(
        code = truncate(item.code, 120),
        name = item.name,
        dock = item.dock,
        createdAt = item.createdAt.toString(),
        timezone = item.timezone,
        odometer = item.odometer.toString(),
        weight = "%.2f".format(item.weight),
        priorityText = item.priority.toString(),
        routeCode = truncate(item.routeCode, 120),
        score = "%.2f".format(item.score),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLockerAllocationViews(items: MutableList<LockerAllocation>): MutableList<LockerAllocationView> {
    val views = mutableListOf<LockerAllocationView>()
    for (item in items) {
        views.add(toLockerAllocationView(item))
    }
    return views
}

fun sumLockerAllocationView(view: LockerAllocationView): Double {
    val parsedCreatedAt = view.createdAt.toInt()
    val parsedOdometer = view.odometer.toInt()
    val parsedWeight = view.weight.toDouble()
    return parsedCreatedAt.toDouble() + parsedOdometer.toDouble() + parsedWeight
}
