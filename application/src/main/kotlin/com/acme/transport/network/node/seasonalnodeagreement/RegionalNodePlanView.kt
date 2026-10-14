package com.acme.transport.network.node.seasonalnodeagreement

import com.acme.transport.kit.text.truncate

/** How screens and exports show a regional node plan. */
data class RegionalNodePlanView(
    var codeText: String,
    var title: String,
    var odometerText: String,
    var palletsText: String,
    var lane: String,
    var visible: String,
    var costText: String,
    var priority: String,
    var limitText: String,
    var statusLabel: String,
)

fun toRegionalNodePlanView(item: RegionalNodePlan): RegionalNodePlanView {
    return RegionalNodePlanView(
        codeText = truncate(item.code, 64),
        title = truncate(item.title, 80),
        odometerText = item.odometer.toString(),
        palletsText = item.pallets.toString(),
        lane = item.lane,
        visible = "${item.visible}",
        costText = "%.2f".format(item.cost),
        priority = item.priority.toString(),
        limitText = item.limit.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRegionalNodePlanViews(items: MutableList<RegionalNodePlan>): MutableList<RegionalNodePlanView> {
    val views = mutableListOf<RegionalNodePlanView>()
    for (item in items) {
        views.add(toRegionalNodePlanView(item))
    }
    return views
}

fun sumRegionalNodePlanView(view: RegionalNodePlanView): Double {
    val parsedOdometer = view.odometerText.toInt()
    val parsedPallets = view.palletsText.toInt()
    val parsedCost = view.costText.toDouble()
    return parsedOdometer.toDouble() + parsedPallets.toDouble() + parsedCost
}
