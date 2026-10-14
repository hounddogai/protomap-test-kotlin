package com.acme.transport.linehaul.backhaul.regionalbackhaultarget

import com.acme.transport.kit.text.truncate

/** How screens and exports show a strict backhaul window. */
data class StrictBackhaulWindowView(
    var code: String,
    var title: String,
    var costText: String,
    var visibleText: String,
    var quantityText: String,
    var statusLabel: String,
)

fun toStrictBackhaulWindowView(item: StrictBackhaulWindow): StrictBackhaulWindowView {
    return StrictBackhaulWindowView(
        code = truncate(item.code, 120),
        title = truncate(item.title, 120),
        costText = "%.2f".format(item.cost),
        visibleText = "${item.visible}",
        quantityText = item.quantity.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toStrictBackhaulWindowViews(items: MutableList<StrictBackhaulWindow>): MutableList<StrictBackhaulWindowView> {
    val views = mutableListOf<StrictBackhaulWindowView>()
    for (item in items) {
        views.add(toStrictBackhaulWindowView(item))
    }
    return views
}

fun sumStrictBackhaulWindowView(view: StrictBackhaulWindowView): Double {
    val parsedCost = view.costText.toDouble()
    val parsedQuantity = view.quantityText.toInt()
    return parsedCost + parsedQuantity.toDouble()
}
