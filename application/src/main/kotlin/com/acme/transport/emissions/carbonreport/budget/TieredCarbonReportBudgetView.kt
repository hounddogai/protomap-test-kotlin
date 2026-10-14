package com.acme.transport.emissions.carbonreport.budget

import com.acme.transport.kit.text.truncate

/** How screens and exports show a tiered carbon report budget. */
data class TieredCarbonReportBudgetView(
    var code: String,
    var title: String,
    var externalRef: String,
    var category: String,
    var sourceText: String,
    var windowDays: String,
    var durationMinutesText: String,
    var statusLabel: String,
)

fun toTieredCarbonReportBudgetView(item: TieredCarbonReportBudget): TieredCarbonReportBudgetView {
    return TieredCarbonReportBudgetView(
        code = item.code,
        title = item.title,
        externalRef = item.externalRef,
        category = item.category,
        sourceText = truncate(item.source, 120),
        windowDays = item.windowDays.toString(),
        durationMinutesText = item.durationMinutes.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTieredCarbonReportBudgetViews(items: MutableList<TieredCarbonReportBudget>): MutableList<TieredCarbonReportBudgetView> {
    val views = mutableListOf<TieredCarbonReportBudgetView>()
    for (item in items) {
        views.add(toTieredCarbonReportBudgetView(item))
    }
    return views
}

fun sumTieredCarbonReportBudgetView(view: TieredCarbonReportBudgetView): Double {
    val parsedWindowDays = view.windowDays.toInt()
    val parsedDurationMinutes = view.durationMinutesText.toInt()
    return parsedWindowDays.toDouble() + parsedDurationMinutes.toDouble()
}
