package com.acme.transport.terminal.dockdoor.score

import com.acme.transport.kit.text.truncate

/** How screens and exports show a dock door adjustment. */
data class DockDoorAdjustmentView(
    var code: String,
    var title: String,
    var quantity: String,
    var cost: String,
    var limit: String,
    var descriptionText: String,
    var statusLabel: String,
)

fun toDockDoorAdjustmentView(item: DockDoorAdjustment): DockDoorAdjustmentView {
    return DockDoorAdjustmentView(
        code = truncate(item.code, 64),
        title = item.title,
        quantity = item.quantity.toString(),
        cost = "%.2f".format(item.cost),
        limit = item.limit.toString(),
        descriptionText = truncate(item.description, 40),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDockDoorAdjustmentViews(items: MutableList<DockDoorAdjustment>): MutableList<DockDoorAdjustmentView> {
    val views = mutableListOf<DockDoorAdjustmentView>()
    for (item in items) {
        views.add(toDockDoorAdjustmentView(item))
    }
    return views
}

fun sumDockDoorAdjustmentView(view: DockDoorAdjustmentView): Double {
    val parsedQuantity = view.quantity.toInt()
    val parsedCost = view.cost.toDouble()
    val parsedLimit = view.limit.toInt()
    return parsedQuantity.toDouble() + parsedCost + parsedLimit.toDouble()
}
