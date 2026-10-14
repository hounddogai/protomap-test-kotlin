package com.acme.transport.terminal.dwell.snapshot

import com.acme.transport.kit.text.truncate

/** How screens and exports show a global dwell budget. */
data class GlobalDwellBudgetView(
    var code: String,
    var label: String,
    var ceiling: String,
    var quantity: String,
    var loadKg: String,
    var unitText: String,
    var contactEmailText: String,
    var graceDays: String,
    var statusLabel: String,
)

fun toGlobalDwellBudgetView(item: GlobalDwellBudget): GlobalDwellBudgetView {
    return GlobalDwellBudgetView(
        code = truncate(item.code, 80),
        label = item.label,
        ceiling = "%.2f".format(item.ceiling),
        quantity = item.quantity.toString(),
        loadKg = "%.2f".format(item.loadKg),
        unitText = truncate(item.unit, 64),
        contactEmailText = item.contactEmail,
        graceDays = item.graceDays.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toGlobalDwellBudgetViews(items: MutableList<GlobalDwellBudget>): MutableList<GlobalDwellBudgetView> {
    val views = mutableListOf<GlobalDwellBudgetView>()
    for (item in items) {
        views.add(toGlobalDwellBudgetView(item))
    }
    return views
}

fun sumGlobalDwellBudgetView(view: GlobalDwellBudgetView): Double {
    val parsedCeiling = view.ceiling.toDouble()
    val parsedQuantity = view.quantity.toInt()
    val parsedLoadKg = view.loadKg.toDouble()
    return parsedCeiling + parsedQuantity.toDouble() + parsedLoadKg
}
