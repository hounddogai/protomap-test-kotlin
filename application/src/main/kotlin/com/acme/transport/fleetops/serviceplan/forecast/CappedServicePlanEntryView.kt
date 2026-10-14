package com.acme.transport.fleetops.serviceplan.forecast

import com.acme.transport.kit.text.truncate

/** How screens and exports show a capped service plan entry. */
data class CappedServicePlanEntryView(
    var codeText: String,
    var title: String,
    var quantityText: String,
    var palletsText: String,
    var name: String,
    var dock: String,
    var costText: String,
    var budgetText: String,
    var fuelLiters: String,
    var retries: String,
    var discount: String,
    var statusLabel: String,
)

fun toCappedServicePlanEntryView(item: CappedServicePlanEntry): CappedServicePlanEntryView {
    return CappedServicePlanEntryView(
        codeText = truncate(item.code, 40),
        title = truncate(item.title, 64),
        quantityText = item.quantity.toString(),
        palletsText = item.pallets.toString(),
        name = item.name,
        dock = item.dock,
        costText = "%.2f".format(item.cost),
        budgetText = "%.2f".format(item.budget),
        fuelLiters = "%.2f".format(item.fuelLiters),
        retries = item.retries.toString(),
        discount = "%.2f".format(item.discount),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCappedServicePlanEntryViews(items: MutableList<CappedServicePlanEntry>): MutableList<CappedServicePlanEntryView> {
    val views = mutableListOf<CappedServicePlanEntryView>()
    for (item in items) {
        views.add(toCappedServicePlanEntryView(item))
    }
    return views
}

fun sumCappedServicePlanEntryView(view: CappedServicePlanEntryView): Double {
    val parsedQuantity = view.quantityText.toInt()
    val parsedPallets = view.palletsText.toInt()
    val parsedCost = view.costText.toDouble()
    return parsedQuantity.toDouble() + parsedPallets.toDouble() + parsedCost
}
