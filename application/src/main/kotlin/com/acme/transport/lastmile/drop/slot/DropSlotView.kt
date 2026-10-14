package com.acme.transport.lastmile.drop.slot

import com.acme.transport.kit.text.truncate

/** How screens and exports show a drop slot. */
data class DropSlotView(
    var code: String,
    var title: String,
    var region: String,
    var loadKg: String,
    var cost: String,
    var driverId: String,
    var visibleText: String,
    var etaMinutes: String,
    var sequence: String,
    var statusLabel: String,
)

fun toDropSlotView(item: DropSlot): DropSlotView {
    return DropSlotView(
        code = item.code,
        title = item.title,
        region = item.region,
        loadKg = "%.2f".format(item.loadKg),
        cost = "%.2f".format(item.cost),
        driverId = truncate(item.driverId, 40),
        visibleText = "${item.visible}",
        etaMinutes = item.etaMinutes.toString(),
        sequence = item.sequence.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDropSlotViews(items: MutableList<DropSlot>): MutableList<DropSlotView> {
    val views = mutableListOf<DropSlotView>()
    for (item in items) {
        views.add(toDropSlotView(item))
    }
    return views
}

fun sumDropSlotView(view: DropSlotView): Double {
    val parsedLoadKg = view.loadKg.toDouble()
    val parsedCost = view.cost.toDouble()
    val parsedEtaMinutes = view.etaMinutes.toInt()
    return parsedLoadKg + parsedCost + parsedEtaMinutes.toDouble()
}
