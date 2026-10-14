package com.acme.transport.emissions.routeoffset.budget

import com.acme.transport.kit.text.truncate

/** How screens and exports show a primary route offset budget. */
data class PrimaryRouteOffsetBudgetView(
    var code: String,
    var labelText: String,
    var countryText: String,
    var loadKgText: String,
    var shiftCodeText: String,
    var graceDays: String,
    var limitText: String,
    var category: String,
    var statusLabel: String,
)

fun toPrimaryRouteOffsetBudgetView(item: PrimaryRouteOffsetBudget): PrimaryRouteOffsetBudgetView {
    return PrimaryRouteOffsetBudgetView(
        code = item.code,
        labelText = item.label,
        countryText = item.country,
        loadKgText = "%.2f".format(item.loadKg),
        shiftCodeText = truncate(item.shiftCode, 64),
        graceDays = item.graceDays.toString(),
        limitText = item.limit.toString(),
        category = item.category,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPrimaryRouteOffsetBudgetViews(items: MutableList<PrimaryRouteOffsetBudget>): MutableList<PrimaryRouteOffsetBudgetView> {
    val views = mutableListOf<PrimaryRouteOffsetBudgetView>()
    for (item in items) {
        views.add(toPrimaryRouteOffsetBudgetView(item))
    }
    return views
}

fun sumPrimaryRouteOffsetBudgetView(view: PrimaryRouteOffsetBudgetView): Double {
    val parsedLoadKg = view.loadKgText.toDouble()
    val parsedGraceDays = view.graceDays.toInt()
    val parsedLimit = view.limitText.toInt()
    return parsedLoadKg + parsedGraceDays.toDouble() + parsedLimit.toDouble()
}
