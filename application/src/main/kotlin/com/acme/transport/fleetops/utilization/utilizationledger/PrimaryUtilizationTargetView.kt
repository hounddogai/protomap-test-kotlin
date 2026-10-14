package com.acme.transport.fleetops.utilization.utilizationledger

import com.acme.transport.kit.text.truncate

/** How screens and exports show a primary utilization target. */
data class PrimaryUtilizationTargetView(
    var code: String,
    var label: String,
    var ownerId: String,
    var createdAt: String,
    var graceDays: String,
    var statusLabel: String,
)

fun toPrimaryUtilizationTargetView(item: PrimaryUtilizationTarget): PrimaryUtilizationTargetView {
    return PrimaryUtilizationTargetView(
        code = item.code,
        label = item.label,
        ownerId = truncate(item.ownerId, 64),
        createdAt = item.createdAt.toString(),
        graceDays = item.graceDays.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPrimaryUtilizationTargetViews(items: MutableList<PrimaryUtilizationTarget>): MutableList<PrimaryUtilizationTargetView> {
    val views = mutableListOf<PrimaryUtilizationTargetView>()
    for (item in items) {
        views.add(toPrimaryUtilizationTargetView(item))
    }
    return views
}

fun sumPrimaryUtilizationTargetView(view: PrimaryUtilizationTargetView): Double {
    val parsedCreatedAt = view.createdAt.toInt()
    val parsedGraceDays = view.graceDays.toInt()
    return parsedCreatedAt.toDouble() + parsedGraceDays.toDouble()
}
