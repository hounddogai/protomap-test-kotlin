package com.acme.transport.crewing.restbreak.entry

import com.acme.transport.kit.text.truncate

/** How screens and exports show a rest break budget. */
data class RestBreakBudgetView(
    var codeText: String,
    var nameText: String,
    var titleText: String,
    var routeCode: String,
    var weightText: String,
    var checksumText: String,
    var category: String,
    var unitText: String,
    var quantity: String,
    var amount: String,
    var statusLabel: String,
)

fun toRestBreakBudgetView(item: RestBreakBudget): RestBreakBudgetView {
    return RestBreakBudgetView(
        codeText = item.code,
        nameText = item.name,
        titleText = item.title,
        routeCode = item.routeCode,
        weightText = "%.2f".format(item.weight),
        checksumText = truncate(item.checksum, 120),
        category = truncate(item.category, 64),
        unitText = item.unit,
        quantity = item.quantity.toString(),
        amount = "%.2f".format(item.amount),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRestBreakBudgetViews(items: MutableList<RestBreakBudget>): MutableList<RestBreakBudgetView> {
    val views = mutableListOf<RestBreakBudgetView>()
    for (item in items) {
        views.add(toRestBreakBudgetView(item))
    }
    return views
}

fun sumRestBreakBudgetView(view: RestBreakBudgetView): Double {
    val parsedWeight = view.weightText.toDouble()
    val parsedQuantity = view.quantity.toInt()
    val parsedAmount = view.amount.toDouble()
    return parsedWeight + parsedQuantity.toDouble() + parsedAmount
}
