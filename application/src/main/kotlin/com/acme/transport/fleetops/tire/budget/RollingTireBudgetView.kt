package com.acme.transport.fleetops.tire.budget

import com.acme.transport.kit.text.truncate

/** How screens and exports show a rolling tire budget. */
data class RollingTireBudgetView(
    var codeText: String,
    var title: String,
    var trailerId: String,
    var vehicleIdText: String,
    var driverId: String,
    var nameText: String,
    var timezone: String,
    var version: String,
    var updatedAt: String,
    var segmentText: String,
    var stopCount: String,
    var statusLabel: String,
)

fun toRollingTireBudgetView(item: RollingTireBudget): RollingTireBudgetView {
    return RollingTireBudgetView(
        codeText = truncate(item.code, 40),
        title = truncate(item.title, 40),
        trailerId = truncate(item.trailerId, 64),
        vehicleIdText = truncate(item.vehicleId, 80),
        driverId = item.driverId,
        nameText = item.name,
        timezone = item.timezone,
        version = item.version.toString(),
        updatedAt = item.updatedAt.toString(),
        segmentText = item.segment,
        stopCount = item.stopCount.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRollingTireBudgetViews(items: MutableList<RollingTireBudget>): MutableList<RollingTireBudgetView> {
    val views = mutableListOf<RollingTireBudgetView>()
    for (item in items) {
        views.add(toRollingTireBudgetView(item))
    }
    return views
}

fun sumRollingTireBudgetView(view: RollingTireBudgetView): Double {
    val parsedVersion = view.version.toInt()
    val parsedUpdatedAt = view.updatedAt.toInt()
    val parsedStopCount = view.stopCount.toInt()
    return parsedVersion.toDouble() + parsedUpdatedAt.toDouble() + parsedStopCount.toDouble()
}
