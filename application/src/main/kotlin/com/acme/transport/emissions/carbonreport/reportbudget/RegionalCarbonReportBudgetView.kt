package com.acme.transport.emissions.carbonreport.reportbudget

import com.acme.transport.kit.text.truncate

/** How screens and exports show a regional carbon report budget. */
data class RegionalCarbonReportBudgetView(
    var code: String,
    var label: String,
    var driverId: String,
    var updatedAt: String,
    var locale: String,
    var discountText: String,
    var statusLabel: String,
)

fun toRegionalCarbonReportBudgetView(item: RegionalCarbonReportBudget): RegionalCarbonReportBudgetView {
    return RegionalCarbonReportBudgetView(
        code = item.code,
        label = truncate(item.label, 80),
        driverId = item.driverId,
        updatedAt = item.updatedAt.toString(),
        locale = item.locale,
        discountText = "%.2f".format(item.discount),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRegionalCarbonReportBudgetViews(items: MutableList<RegionalCarbonReportBudget>): MutableList<RegionalCarbonReportBudgetView> {
    val views = mutableListOf<RegionalCarbonReportBudgetView>()
    for (item in items) {
        views.add(toRegionalCarbonReportBudgetView(item))
    }
    return views
}

fun sumRegionalCarbonReportBudgetView(view: RegionalCarbonReportBudgetView): Double {
    val parsedUpdatedAt = view.updatedAt.toInt()
    val parsedDiscount = view.discountText.toDouble()
    return parsedUpdatedAt.toDouble() + parsedDiscount
}
