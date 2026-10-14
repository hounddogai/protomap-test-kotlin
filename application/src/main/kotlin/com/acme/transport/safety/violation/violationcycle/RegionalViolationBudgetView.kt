package com.acme.transport.safety.violation.violationcycle

import com.acme.transport.kit.text.truncate

/** How screens and exports show a regional violation budget. */
data class RegionalViolationBudgetView(
    var code: String,
    var name: String,
    var capacity: String,
    var ownerIdText: String,
    var carrier: String,
    var rate: String,
    var currency: String,
    var dock: String,
    var statusLabel: String,
)

fun toRegionalViolationBudgetView(item: RegionalViolationBudget): RegionalViolationBudgetView {
    return RegionalViolationBudgetView(
        code = item.code,
        name = item.name,
        capacity = item.capacity.toString(),
        ownerIdText = item.ownerId,
        carrier = item.carrier,
        rate = "%.2f".format(item.rate),
        currency = truncate(item.currency, 40),
        dock = item.dock,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRegionalViolationBudgetViews(items: MutableList<RegionalViolationBudget>): MutableList<RegionalViolationBudgetView> {
    val views = mutableListOf<RegionalViolationBudgetView>()
    for (item in items) {
        views.add(toRegionalViolationBudgetView(item))
    }
    return views
}

fun sumRegionalViolationBudgetView(view: RegionalViolationBudgetView): Double {
    val parsedCapacity = view.capacity.toInt()
    val parsedRate = view.rate.toDouble()
    return parsedCapacity.toDouble() + parsedRate
}
