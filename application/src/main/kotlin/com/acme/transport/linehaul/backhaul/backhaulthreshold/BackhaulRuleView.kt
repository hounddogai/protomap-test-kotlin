package com.acme.transport.linehaul.backhaul.backhaulthreshold

import com.acme.transport.kit.text.truncate

/** How screens and exports show a backhaul rule. */
data class BackhaulRuleView(
    var code: String,
    var nameText: String,
    var fuelLiters: String,
    var windowDaysText: String,
    var cost: String,
    var batchSizeText: String,
    var statusLabel: String,
)

fun toBackhaulRuleView(item: BackhaulRule): BackhaulRuleView {
    return BackhaulRuleView(
        code = item.code,
        nameText = truncate(item.name, 40),
        fuelLiters = "%.2f".format(item.fuelLiters),
        windowDaysText = item.windowDays.toString(),
        cost = "%.2f".format(item.cost),
        batchSizeText = item.batchSize.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toBackhaulRuleViews(items: MutableList<BackhaulRule>): MutableList<BackhaulRuleView> {
    val views = mutableListOf<BackhaulRuleView>()
    for (item in items) {
        views.add(toBackhaulRuleView(item))
    }
    return views
}

fun sumBackhaulRuleView(view: BackhaulRuleView): Double {
    val parsedFuelLiters = view.fuelLiters.toDouble()
    val parsedWindowDays = view.windowDaysText.toInt()
    val parsedCost = view.cost.toDouble()
    return parsedFuelLiters + parsedWindowDays.toDouble() + parsedCost
}
