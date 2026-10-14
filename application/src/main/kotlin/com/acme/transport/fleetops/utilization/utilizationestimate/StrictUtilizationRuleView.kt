package com.acme.transport.fleetops.utilization.utilizationestimate

import com.acme.transport.kit.text.truncate

/** How screens and exports show a strict utilization rule. */
data class StrictUtilizationRuleView(
    var code: String,
    var title: String,
    var budgetText: String,
    var priorityText: String,
    var floorValueText: String,
    var version: String,
    var statusLabel: String,
)

fun toStrictUtilizationRuleView(item: StrictUtilizationRule): StrictUtilizationRuleView {
    return StrictUtilizationRuleView(
        code = truncate(item.code, 40),
        title = item.title,
        budgetText = "%.2f".format(item.budget),
        priorityText = item.priority.toString(),
        floorValueText = "%.2f".format(item.floorValue),
        version = item.version.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toStrictUtilizationRuleViews(items: MutableList<StrictUtilizationRule>): MutableList<StrictUtilizationRuleView> {
    val views = mutableListOf<StrictUtilizationRuleView>()
    for (item in items) {
        views.add(toStrictUtilizationRuleView(item))
    }
    return views
}

fun sumStrictUtilizationRuleView(view: StrictUtilizationRuleView): Double {
    val parsedBudget = view.budgetText.toDouble()
    val parsedPriority = view.priorityText.toInt()
    val parsedFloorValue = view.floorValueText.toDouble()
    return parsedBudget + parsedPriority.toDouble() + parsedFloorValue
}
