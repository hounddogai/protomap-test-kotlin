package com.acme.transport.terminal.traileryard.reservation

import com.acme.transport.kit.text.truncate

/** How screens and exports show a rolling trailer yard allocation. */
data class RollingTrailerYardAllocationView(
    var code: String,
    var name: String,
    var budgetText: String,
    var ratioText: String,
    var priority: String,
    var lane: String,
    var floorValue: String,
    var statusLabel: String,
)

fun toRollingTrailerYardAllocationView(item: RollingTrailerYardAllocation): RollingTrailerYardAllocationView {
    return RollingTrailerYardAllocationView(
        code = item.code,
        name = truncate(item.name, 40),
        budgetText = "%.2f".format(item.budget),
        ratioText = "%.2f".format(item.ratio),
        priority = item.priority.toString(),
        lane = item.lane,
        floorValue = "%.2f".format(item.floorValue),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRollingTrailerYardAllocationViews(items: MutableList<RollingTrailerYardAllocation>): MutableList<RollingTrailerYardAllocationView> {
    val views = mutableListOf<RollingTrailerYardAllocationView>()
    for (item in items) {
        views.add(toRollingTrailerYardAllocationView(item))
    }
    return views
}

fun sumRollingTrailerYardAllocationView(view: RollingTrailerYardAllocationView): Double {
    val parsedBudget = view.budgetText.toDouble()
    val parsedRatio = view.ratioText.toDouble()
    val parsedPriority = view.priority.toInt()
    return parsedBudget + parsedRatio + parsedPriority.toDouble()
}
