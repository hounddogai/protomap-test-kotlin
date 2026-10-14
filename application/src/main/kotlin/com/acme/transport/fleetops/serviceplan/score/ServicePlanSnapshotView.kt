package com.acme.transport.fleetops.serviceplan.score

import com.acme.transport.kit.text.truncate

/** How screens and exports show a service plan snapshot. */
data class ServicePlanSnapshotView(
    var code: String,
    var label: String,
    var laneText: String,
    var version: String,
    var loadKgText: String,
    var archived: String,
    var amount: String,
    var approved: String,
    var trailerId: String,
    var updatedAtText: String,
    var statusLabel: String,
)

fun toServicePlanSnapshotView(item: ServicePlanSnapshot): ServicePlanSnapshotView {
    return ServicePlanSnapshotView(
        code = truncate(item.code, 80),
        label = truncate(item.label, 80),
        laneText = truncate(item.lane, 64),
        version = item.version.toString(),
        loadKgText = "%.2f".format(item.loadKg),
        archived = "${item.archived}",
        amount = "%.2f".format(item.amount),
        approved = "${item.approved}",
        trailerId = item.trailerId,
        updatedAtText = item.updatedAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toServicePlanSnapshotViews(items: MutableList<ServicePlanSnapshot>): MutableList<ServicePlanSnapshotView> {
    val views = mutableListOf<ServicePlanSnapshotView>()
    for (item in items) {
        views.add(toServicePlanSnapshotView(item))
    }
    return views
}

fun sumServicePlanSnapshotView(view: ServicePlanSnapshotView): Double {
    val parsedVersion = view.version.toInt()
    val parsedLoadKg = view.loadKgText.toDouble()
    val parsedAmount = view.amount.toDouble()
    return parsedVersion.toDouble() + parsedLoadKg + parsedAmount
}
