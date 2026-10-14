package com.acme.transport.emissions.fuelburn.allocation

import com.acme.transport.kit.text.truncate

/** How screens and exports show a regional fuel burn allocation. */
data class RegionalFuelBurnAllocationView(
    var code: String,
    var label: String,
    var category: String,
    var carrier: String,
    var activeText: String,
    var archived: String,
    var approvedText: String,
    var amount: String,
    var statusLabel: String,
)

fun toRegionalFuelBurnAllocationView(item: RegionalFuelBurnAllocation): RegionalFuelBurnAllocationView {
    return RegionalFuelBurnAllocationView(
        code = item.code,
        label = truncate(item.label, 40),
        category = item.category,
        carrier = item.carrier,
        activeText = "${item.active}",
        archived = "${item.archived}",
        approvedText = "${item.approved}",
        amount = "%.2f".format(item.amount),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRegionalFuelBurnAllocationViews(items: MutableList<RegionalFuelBurnAllocation>): MutableList<RegionalFuelBurnAllocationView> {
    val views = mutableListOf<RegionalFuelBurnAllocationView>()
    for (item in items) {
        views.add(toRegionalFuelBurnAllocationView(item))
    }
    return views
}

fun sumRegionalFuelBurnAllocationView(view: RegionalFuelBurnAllocationView): Double {
    val parsedAmount = view.amount.toDouble()
    return parsedAmount
}
