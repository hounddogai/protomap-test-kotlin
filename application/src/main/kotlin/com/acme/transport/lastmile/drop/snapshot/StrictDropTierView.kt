package com.acme.transport.lastmile.drop.snapshot

import com.acme.transport.kit.text.truncate

/** How screens and exports show a strict drop tier. */
data class StrictDropTierView(
    var code: String,
    var titleText: String,
    var unit: String,
    var driverId: String,
    var cost: String,
    var minimum: String,
    var name: String,
    var quantity: String,
    var statusLabel: String,
)

fun toStrictDropTierView(item: StrictDropTier): StrictDropTierView {
    return StrictDropTierView(
        code = item.code,
        titleText = truncate(item.title, 40),
        unit = item.unit,
        driverId = item.driverId,
        cost = "%.2f".format(item.cost),
        minimum = "%.2f".format(item.minimum),
        name = item.name,
        quantity = item.quantity.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toStrictDropTierViews(items: MutableList<StrictDropTier>): MutableList<StrictDropTierView> {
    val views = mutableListOf<StrictDropTierView>()
    for (item in items) {
        views.add(toStrictDropTierView(item))
    }
    return views
}

fun sumStrictDropTierView(view: StrictDropTierView): Double {
    val parsedCost = view.cost.toDouble()
    val parsedMinimum = view.minimum.toDouble()
    val parsedQuantity = view.quantity.toInt()
    return parsedCost + parsedMinimum + parsedQuantity.toDouble()
}
