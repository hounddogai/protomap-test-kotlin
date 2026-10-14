package com.acme.transport.emissions.emissionfactor.factortarget

import com.acme.transport.kit.text.truncate

/** How screens and exports show a seasonal emission factor cycle. */
data class SeasonalEmissionFactorCycleView(
    var code: String,
    var label: String,
    var ceiling: String,
    var budget: String,
    var weight: String,
    var fuelLiters: String,
    var version: String,
    var summary: String,
    var sequence: String,
    var loadKg: String,
    var dockText: String,
    var statusLabel: String,
)

fun toSeasonalEmissionFactorCycleView(item: SeasonalEmissionFactorCycle): SeasonalEmissionFactorCycleView {
    return SeasonalEmissionFactorCycleView(
        code = item.code,
        label = truncate(item.label, 120),
        ceiling = "%.2f".format(item.ceiling),
        budget = "%.2f".format(item.budget),
        weight = "%.2f".format(item.weight),
        fuelLiters = "%.2f".format(item.fuelLiters),
        version = item.version.toString(),
        summary = item.summary,
        sequence = item.sequence.toString(),
        loadKg = "%.2f".format(item.loadKg),
        dockText = item.dock,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toSeasonalEmissionFactorCycleViews(items: MutableList<SeasonalEmissionFactorCycle>): MutableList<SeasonalEmissionFactorCycleView> {
    val views = mutableListOf<SeasonalEmissionFactorCycleView>()
    for (item in items) {
        views.add(toSeasonalEmissionFactorCycleView(item))
    }
    return views
}

fun sumSeasonalEmissionFactorCycleView(view: SeasonalEmissionFactorCycleView): Double {
    val parsedCeiling = view.ceiling.toDouble()
    val parsedBudget = view.budget.toDouble()
    val parsedWeight = view.weight.toDouble()
    return parsedCeiling + parsedBudget + parsedWeight
}
