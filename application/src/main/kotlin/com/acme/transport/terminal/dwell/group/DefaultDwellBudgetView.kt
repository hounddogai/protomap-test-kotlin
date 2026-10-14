package com.acme.transport.terminal.dwell.group

import com.acme.transport.kit.text.truncate

/** How screens and exports show a default dwell budget. */
data class DefaultDwellBudgetView(
    var code: String,
    var title: String,
    var weightText: String,
    var budgetText: String,
    var active: String,
    var approved: String,
    var reviewer: String,
    var minimum: String,
    var statusLabel: String,
)

fun toDefaultDwellBudgetView(item: DefaultDwellBudget): DefaultDwellBudgetView {
    return DefaultDwellBudgetView(
        code = item.code,
        title = item.title,
        weightText = "%.2f".format(item.weight),
        budgetText = "%.2f".format(item.budget),
        active = "${item.active}",
        approved = "${item.approved}",
        reviewer = truncate(item.reviewer, 64),
        minimum = "%.2f".format(item.minimum),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDefaultDwellBudgetViews(items: MutableList<DefaultDwellBudget>): MutableList<DefaultDwellBudgetView> {
    val views = mutableListOf<DefaultDwellBudgetView>()
    for (item in items) {
        views.add(toDefaultDwellBudgetView(item))
    }
    return views
}

fun sumDefaultDwellBudgetView(view: DefaultDwellBudgetView): Double {
    val parsedWeight = view.weightText.toDouble()
    val parsedBudget = view.budgetText.toDouble()
    val parsedMinimum = view.minimum.toDouble()
    return parsedWeight + parsedBudget + parsedMinimum
}
