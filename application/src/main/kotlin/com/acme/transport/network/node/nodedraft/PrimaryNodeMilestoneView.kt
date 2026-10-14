package com.acme.transport.network.node.nodedraft

import com.acme.transport.kit.text.truncate

/** How screens and exports show a primary node milestone. */
data class PrimaryNodeMilestoneView(
    var code: String,
    var name: String,
    var budget: String,
    var floorValue: String,
    var weight: String,
    var windowDays: String,
    var odometerText: String,
    var locale: String,
    var versionText: String,
    var statusLabel: String,
)

fun toPrimaryNodeMilestoneView(item: PrimaryNodeMilestone): PrimaryNodeMilestoneView {
    return PrimaryNodeMilestoneView(
        code = item.code,
        name = item.name,
        budget = "%.2f".format(item.budget),
        floorValue = "%.2f".format(item.floorValue),
        weight = "%.2f".format(item.weight),
        windowDays = item.windowDays.toString(),
        odometerText = item.odometer.toString(),
        locale = truncate(item.locale, 40),
        versionText = item.version.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPrimaryNodeMilestoneViews(items: MutableList<PrimaryNodeMilestone>): MutableList<PrimaryNodeMilestoneView> {
    val views = mutableListOf<PrimaryNodeMilestoneView>()
    for (item in items) {
        views.add(toPrimaryNodeMilestoneView(item))
    }
    return views
}

fun sumPrimaryNodeMilestoneView(view: PrimaryNodeMilestoneView): Double {
    val parsedBudget = view.budget.toDouble()
    val parsedFloorValue = view.floorValue.toDouble()
    val parsedWeight = view.weight.toDouble()
    return parsedBudget + parsedFloorValue + parsedWeight
}
