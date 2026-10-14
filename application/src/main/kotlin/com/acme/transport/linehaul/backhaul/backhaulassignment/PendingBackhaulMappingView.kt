package com.acme.transport.linehaul.backhaul.backhaulassignment

import com.acme.transport.kit.text.truncate

/** How screens and exports show a pending backhaul mapping. */
data class PendingBackhaulMappingView(
    var codeText: String,
    var name: String,
    var updatedAtText: String,
    var priority: String,
    var cost: String,
    var palletsText: String,
    var capacityText: String,
    var fuelLiters: String,
    var statusLabel: String,
)

fun toPendingBackhaulMappingView(item: PendingBackhaulMapping): PendingBackhaulMappingView {
    return PendingBackhaulMappingView(
        codeText = truncate(item.code, 80),
        name = item.name,
        updatedAtText = item.updatedAt.toString(),
        priority = item.priority.toString(),
        cost = "%.2f".format(item.cost),
        palletsText = item.pallets.toString(),
        capacityText = item.capacity.toString(),
        fuelLiters = "%.2f".format(item.fuelLiters),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPendingBackhaulMappingViews(items: MutableList<PendingBackhaulMapping>): MutableList<PendingBackhaulMappingView> {
    val views = mutableListOf<PendingBackhaulMappingView>()
    for (item in items) {
        views.add(toPendingBackhaulMappingView(item))
    }
    return views
}

fun sumPendingBackhaulMappingView(view: PendingBackhaulMappingView): Double {
    val parsedUpdatedAt = view.updatedAtText.toInt()
    val parsedPriority = view.priority.toInt()
    val parsedCost = view.cost.toDouble()
    return parsedUpdatedAt.toDouble() + parsedPriority.toDouble() + parsedCost
}
