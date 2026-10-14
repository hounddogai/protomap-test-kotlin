package com.acme.transport.fleetops.utilization.utilizationledger

import com.acme.transport.kit.text.truncate

/** How screens and exports show a regional utilization cycle. */
data class RegionalUtilizationCycleView(
    var code: String,
    var title: String,
    var amountText: String,
    var active: String,
    var notes: String,
    var priorityText: String,
    var discountText: String,
    var revision: String,
    var statusLabel: String,
)

fun toRegionalUtilizationCycleView(item: RegionalUtilizationCycle): RegionalUtilizationCycleView {
    return RegionalUtilizationCycleView(
        code = item.code,
        title = truncate(item.title, 120),
        amountText = "%.2f".format(item.amount),
        active = "${item.active}",
        notes = truncate(item.notes, 64),
        priorityText = item.priority.toString(),
        discountText = "%.2f".format(item.discount),
        revision = item.revision.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRegionalUtilizationCycleViews(items: MutableList<RegionalUtilizationCycle>): MutableList<RegionalUtilizationCycleView> {
    val views = mutableListOf<RegionalUtilizationCycleView>()
    for (item in items) {
        views.add(toRegionalUtilizationCycleView(item))
    }
    return views
}

fun sumRegionalUtilizationCycleView(view: RegionalUtilizationCycleView): Double {
    val parsedAmount = view.amountText.toDouble()
    val parsedPriority = view.priorityText.toInt()
    val parsedDiscount = view.discountText.toDouble()
    return parsedAmount + parsedPriority.toDouble() + parsedDiscount
}
