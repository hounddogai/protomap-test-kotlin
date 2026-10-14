package com.acme.transport.emissions.fuelburn.burnbudget

import com.acme.transport.kit.text.truncate

/** How screens and exports show a capped fuel burn budget. */
data class CappedFuelBurnBudgetView(
    var codeText: String,
    var title: String,
    var archivedText: String,
    var timezone: String,
    var pallets: String,
    var updatedAt: String,
    var weight: String,
    var shiftCodeText: String,
    var statusLabel: String,
)

fun toCappedFuelBurnBudgetView(item: CappedFuelBurnBudget): CappedFuelBurnBudgetView {
    return CappedFuelBurnBudgetView(
        codeText = truncate(item.code, 120),
        title = item.title,
        archivedText = "${item.archived}",
        timezone = item.timezone,
        pallets = item.pallets.toString(),
        updatedAt = item.updatedAt.toString(),
        weight = "%.2f".format(item.weight),
        shiftCodeText = item.shiftCode,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCappedFuelBurnBudgetViews(items: MutableList<CappedFuelBurnBudget>): MutableList<CappedFuelBurnBudgetView> {
    val views = mutableListOf<CappedFuelBurnBudgetView>()
    for (item in items) {
        views.add(toCappedFuelBurnBudgetView(item))
    }
    return views
}

fun sumCappedFuelBurnBudgetView(view: CappedFuelBurnBudgetView): Double {
    val parsedPallets = view.pallets.toInt()
    val parsedUpdatedAt = view.updatedAt.toInt()
    val parsedWeight = view.weight.toDouble()
    return parsedPallets.toDouble() + parsedUpdatedAt.toDouble() + parsedWeight
}
