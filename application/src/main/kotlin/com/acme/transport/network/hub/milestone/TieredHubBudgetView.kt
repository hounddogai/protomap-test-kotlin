package com.acme.transport.network.hub.milestone

import com.acme.transport.kit.text.truncate

/** How screens and exports show a tiered hub budget. */
data class TieredHubBudgetView(
    var code: String,
    var titleText: String,
    var label: String,
    var odometer: String,
    var dockText: String,
    var shiftCode: String,
    var budget: String,
    var retries: String,
    var amountText: String,
    var statusLabel: String,
)

fun toTieredHubBudgetView(item: TieredHubBudget): TieredHubBudgetView {
    return TieredHubBudgetView(
        code = item.code,
        titleText = truncate(item.title, 120),
        label = item.label,
        odometer = item.odometer.toString(),
        dockText = item.dock,
        shiftCode = truncate(item.shiftCode, 80),
        budget = "%.2f".format(item.budget),
        retries = item.retries.toString(),
        amountText = "%.2f".format(item.amount),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTieredHubBudgetViews(items: MutableList<TieredHubBudget>): MutableList<TieredHubBudgetView> {
    val views = mutableListOf<TieredHubBudgetView>()
    for (item in items) {
        views.add(toTieredHubBudgetView(item))
    }
    return views
}

fun sumTieredHubBudgetView(view: TieredHubBudgetView): Double {
    val parsedOdometer = view.odometer.toInt()
    val parsedBudget = view.budget.toDouble()
    val parsedRetries = view.retries.toInt()
    return parsedOdometer.toDouble() + parsedBudget + parsedRetries.toDouble()
}
