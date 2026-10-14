package com.acme.transport.emissions.fuelburn.claim

import com.acme.transport.kit.text.truncate

/** How screens and exports show a weighted fuel burn budget. */
data class WeightedFuelBurnBudgetView(
    var code: String,
    var name: String,
    var ceiling: String,
    var active: String,
    var weightText: String,
    var locale: String,
    var sourceText: String,
    var odometerText: String,
    var statusLabel: String,
)

fun toWeightedFuelBurnBudgetView(item: WeightedFuelBurnBudget): WeightedFuelBurnBudgetView {
    return WeightedFuelBurnBudgetView(
        code = item.code,
        name = truncate(item.name, 120),
        ceiling = "%.2f".format(item.ceiling),
        active = "${item.active}",
        weightText = "%.2f".format(item.weight),
        locale = item.locale,
        sourceText = item.source,
        odometerText = item.odometer.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toWeightedFuelBurnBudgetViews(items: MutableList<WeightedFuelBurnBudget>): MutableList<WeightedFuelBurnBudgetView> {
    val views = mutableListOf<WeightedFuelBurnBudgetView>()
    for (item in items) {
        views.add(toWeightedFuelBurnBudgetView(item))
    }
    return views
}

fun sumWeightedFuelBurnBudgetView(view: WeightedFuelBurnBudgetView): Double {
    val parsedCeiling = view.ceiling.toDouble()
    val parsedWeight = view.weightText.toDouble()
    val parsedOdometer = view.odometerText.toInt()
    return parsedCeiling + parsedWeight + parsedOdometer.toDouble()
}
