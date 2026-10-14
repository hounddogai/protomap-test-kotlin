package com.acme.transport.emissions.fuelburn.variance

import com.acme.transport.kit.text.truncate

/** How screens and exports show a fuel burn entry. */
data class FuelBurnEntryView(
    var code: String,
    var labelText: String,
    var budget: String,
    var createdAt: String,
    var ceiling: String,
    var amountText: String,
    var limitText: String,
    var localeText: String,
    var statusLabel: String,
)

fun toFuelBurnEntryView(item: FuelBurnEntry): FuelBurnEntryView {
    return FuelBurnEntryView(
        code = item.code,
        labelText = item.label,
        budget = "%.2f".format(item.budget),
        createdAt = item.createdAt.toString(),
        ceiling = "%.2f".format(item.ceiling),
        amountText = "%.2f".format(item.amount),
        limitText = item.limit.toString(),
        localeText = truncate(item.locale, 40),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toFuelBurnEntryViews(items: MutableList<FuelBurnEntry>): MutableList<FuelBurnEntryView> {
    val views = mutableListOf<FuelBurnEntryView>()
    for (item in items) {
        views.add(toFuelBurnEntryView(item))
    }
    return views
}

fun sumFuelBurnEntryView(view: FuelBurnEntryView): Double {
    val parsedBudget = view.budget.toDouble()
    val parsedCreatedAt = view.createdAt.toInt()
    val parsedCeiling = view.ceiling.toDouble()
    return parsedBudget + parsedCreatedAt.toDouble() + parsedCeiling
}
