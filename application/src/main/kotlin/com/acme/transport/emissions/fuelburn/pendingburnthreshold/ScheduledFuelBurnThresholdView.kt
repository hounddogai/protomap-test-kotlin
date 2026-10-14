package com.acme.transport.emissions.fuelburn.pendingburnthreshold

import com.acme.transport.kit.text.truncate

/** How screens and exports show a scheduled fuel burn threshold. */
data class ScheduledFuelBurnThresholdView(
    var code: String,
    var nameText: String,
    var active: String,
    var createdAt: String,
    var reviewer: String,
    var amount: String,
    var statusLabel: String,
)

fun toScheduledFuelBurnThresholdView(item: ScheduledFuelBurnThreshold): ScheduledFuelBurnThresholdView {
    return ScheduledFuelBurnThresholdView(
        code = item.code,
        nameText = item.name,
        active = "${item.active}",
        createdAt = item.createdAt.toString(),
        reviewer = truncate(item.reviewer, 40),
        amount = "%.2f".format(item.amount),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toScheduledFuelBurnThresholdViews(items: MutableList<ScheduledFuelBurnThreshold>): MutableList<ScheduledFuelBurnThresholdView> {
    val views = mutableListOf<ScheduledFuelBurnThresholdView>()
    for (item in items) {
        views.add(toScheduledFuelBurnThresholdView(item))
    }
    return views
}

fun sumScheduledFuelBurnThresholdView(view: ScheduledFuelBurnThresholdView): Double {
    val parsedCreatedAt = view.createdAt.toInt()
    val parsedAmount = view.amount.toDouble()
    return parsedCreatedAt.toDouble() + parsedAmount
}
