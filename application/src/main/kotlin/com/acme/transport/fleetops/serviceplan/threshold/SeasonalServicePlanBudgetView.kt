package com.acme.transport.fleetops.serviceplan.threshold

import com.acme.transport.kit.text.truncate

/** How screens and exports show a seasonal service plan budget. */
data class SeasonalServicePlanBudgetView(
    var code: String,
    var name: String,
    var category: String,
    var shiftCode: String,
    var quantityText: String,
    var revisionText: String,
    var labelText: String,
    var stopCountText: String,
    var statusLabel: String,
)

fun toSeasonalServicePlanBudgetView(item: SeasonalServicePlanBudget): SeasonalServicePlanBudgetView {
    return SeasonalServicePlanBudgetView(
        code = truncate(item.code, 80),
        name = item.name,
        category = truncate(item.category, 120),
        shiftCode = item.shiftCode,
        quantityText = item.quantity.toString(),
        revisionText = item.revision.toString(),
        labelText = item.label,
        stopCountText = item.stopCount.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toSeasonalServicePlanBudgetViews(items: MutableList<SeasonalServicePlanBudget>): MutableList<SeasonalServicePlanBudgetView> {
    val views = mutableListOf<SeasonalServicePlanBudgetView>()
    for (item in items) {
        views.add(toSeasonalServicePlanBudgetView(item))
    }
    return views
}

fun sumSeasonalServicePlanBudgetView(view: SeasonalServicePlanBudgetView): Double {
    val parsedQuantity = view.quantityText.toInt()
    val parsedRevision = view.revisionText.toInt()
    val parsedStopCount = view.stopCountText.toInt()
    return parsedQuantity.toDouble() + parsedRevision.toDouble() + parsedStopCount.toDouble()
}
