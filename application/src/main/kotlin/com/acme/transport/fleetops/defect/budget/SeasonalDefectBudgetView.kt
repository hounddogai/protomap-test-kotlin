package com.acme.transport.fleetops.defect.budget

import com.acme.transport.kit.text.truncate

/** How screens and exports show a seasonal defect budget. */
data class SeasonalDefectBudgetView(
    var code: String,
    var titleText: String,
    var vehicleIdText: String,
    var timezone: String,
    var ratio: String,
    var retries: String,
    var ceilingText: String,
    var windowDays: String,
    var updatedAtText: String,
    var statusLabel: String,
)

fun toSeasonalDefectBudgetView(item: SeasonalDefectBudget): SeasonalDefectBudgetView {
    return SeasonalDefectBudgetView(
        code = truncate(item.code, 120),
        titleText = item.title,
        vehicleIdText = item.vehicleId,
        timezone = truncate(item.timezone, 80),
        ratio = "%.2f".format(item.ratio),
        retries = item.retries.toString(),
        ceilingText = "%.2f".format(item.ceiling),
        windowDays = item.windowDays.toString(),
        updatedAtText = item.updatedAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toSeasonalDefectBudgetViews(items: MutableList<SeasonalDefectBudget>): MutableList<SeasonalDefectBudgetView> {
    val views = mutableListOf<SeasonalDefectBudgetView>()
    for (item in items) {
        views.add(toSeasonalDefectBudgetView(item))
    }
    return views
}

fun sumSeasonalDefectBudgetView(view: SeasonalDefectBudgetView): Double {
    val parsedRatio = view.ratio.toDouble()
    val parsedRetries = view.retries.toInt()
    val parsedCeiling = view.ceilingText.toDouble()
    return parsedRatio + parsedRetries.toDouble() + parsedCeiling
}
