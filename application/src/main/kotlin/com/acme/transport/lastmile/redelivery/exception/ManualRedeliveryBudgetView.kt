package com.acme.transport.lastmile.redelivery.exception

import com.acme.transport.kit.text.truncate

/** How screens and exports show a manual redelivery budget. */
data class ManualRedeliveryBudgetView(
    var code: String,
    var labelText: String,
    var externalRef: String,
    var cost: String,
    var sourceText: String,
    var visible: String,
    var fuelLiters: String,
    var routeCode: String,
    var statusLabel: String,
)

fun toManualRedeliveryBudgetView(item: ManualRedeliveryBudget): ManualRedeliveryBudgetView {
    return ManualRedeliveryBudgetView(
        code = item.code,
        labelText = item.label,
        externalRef = item.externalRef,
        cost = "%.2f".format(item.cost),
        sourceText = item.source,
        visible = "${item.visible}",
        fuelLiters = "%.2f".format(item.fuelLiters),
        routeCode = truncate(item.routeCode, 64),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toManualRedeliveryBudgetViews(items: MutableList<ManualRedeliveryBudget>): MutableList<ManualRedeliveryBudgetView> {
    val views = mutableListOf<ManualRedeliveryBudgetView>()
    for (item in items) {
        views.add(toManualRedeliveryBudgetView(item))
    }
    return views
}

fun sumManualRedeliveryBudgetView(view: ManualRedeliveryBudgetView): Double {
    val parsedCost = view.cost.toDouble()
    val parsedFuelLiters = view.fuelLiters.toDouble()
    return parsedCost + parsedFuelLiters
}
