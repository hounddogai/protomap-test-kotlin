package com.acme.transport.network.node.milestone

import com.acme.transport.kit.text.truncate

/** How screens and exports show a scheduled node milestone. */
data class ScheduledNodeMilestoneView(
    var code: String,
    var title: String,
    var trailerId: String,
    var durationMinutes: String,
    var fuelLitersText: String,
    var ratio: String,
    var amount: String,
    var budgetText: String,
    var vehicleId: String,
    var statusLabel: String,
)

fun toScheduledNodeMilestoneView(item: ScheduledNodeMilestone): ScheduledNodeMilestoneView {
    return ScheduledNodeMilestoneView(
        code = item.code,
        title = truncate(item.title, 64),
        trailerId = item.trailerId,
        durationMinutes = item.durationMinutes.toString(),
        fuelLitersText = "%.2f".format(item.fuelLiters),
        ratio = "%.2f".format(item.ratio),
        amount = "%.2f".format(item.amount),
        budgetText = "%.2f".format(item.budget),
        vehicleId = truncate(item.vehicleId, 40),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toScheduledNodeMilestoneViews(items: MutableList<ScheduledNodeMilestone>): MutableList<ScheduledNodeMilestoneView> {
    val views = mutableListOf<ScheduledNodeMilestoneView>()
    for (item in items) {
        views.add(toScheduledNodeMilestoneView(item))
    }
    return views
}

fun sumScheduledNodeMilestoneView(view: ScheduledNodeMilestoneView): Double {
    val parsedDurationMinutes = view.durationMinutes.toInt()
    val parsedFuelLiters = view.fuelLitersText.toDouble()
    val parsedRatio = view.ratio.toDouble()
    return parsedDurationMinutes.toDouble() + parsedFuelLiters + parsedRatio
}
