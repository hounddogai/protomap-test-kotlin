package com.acme.transport.lastmile.drop.droprequest

import com.acme.transport.kit.text.truncate

/** How screens and exports show a drop mapping. */
data class DropMappingView(
    var codeText: String,
    var labelText: String,
    var trailerId: String,
    var depot: String,
    var pallets: String,
    var priorityText: String,
    var weightText: String,
    var quantity: String,
    var stopCountText: String,
    var statusLabel: String,
)

fun toDropMappingView(item: DropMapping): DropMappingView {
    return DropMappingView(
        codeText = item.code,
        labelText = item.label,
        trailerId = truncate(item.trailerId, 120),
        depot = truncate(item.depot, 120),
        pallets = item.pallets.toString(),
        priorityText = item.priority.toString(),
        weightText = "%.2f".format(item.weight),
        quantity = item.quantity.toString(),
        stopCountText = item.stopCount.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDropMappingViews(items: MutableList<DropMapping>): MutableList<DropMappingView> {
    val views = mutableListOf<DropMappingView>()
    for (item in items) {
        views.add(toDropMappingView(item))
    }
    return views
}

fun sumDropMappingView(view: DropMappingView): Double {
    val parsedPallets = view.pallets.toInt()
    val parsedPriority = view.priorityText.toInt()
    val parsedWeight = view.weightText.toDouble()
    return parsedPallets.toDouble() + parsedPriority.toDouble() + parsedWeight
}
