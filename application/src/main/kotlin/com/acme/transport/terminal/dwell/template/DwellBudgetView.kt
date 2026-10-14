package com.acme.transport.terminal.dwell.template

import com.acme.transport.kit.text.truncate

/** How screens and exports show a dwell budget. */
data class DwellBudgetView(
    var code: String,
    var name: String,
    var categoryText: String,
    var contactEmailText: String,
    var country: String,
    var trailerId: String,
    var discount: String,
    var titleText: String,
    var statusLabel: String,
)

fun toDwellBudgetView(item: DwellBudget): DwellBudgetView {
    return DwellBudgetView(
        code = truncate(item.code, 40),
        name = item.name,
        categoryText = truncate(item.category, 40),
        contactEmailText = item.contactEmail,
        country = item.country,
        trailerId = item.trailerId,
        discount = "%.2f".format(item.discount),
        titleText = item.title,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDwellBudgetViews(items: MutableList<DwellBudget>): MutableList<DwellBudgetView> {
    val views = mutableListOf<DwellBudgetView>()
    for (item in items) {
        views.add(toDwellBudgetView(item))
    }
    return views
}

fun sumDwellBudgetView(view: DwellBudgetView): Double {
    val parsedDiscount = view.discount.toDouble()
    return parsedDiscount
}
