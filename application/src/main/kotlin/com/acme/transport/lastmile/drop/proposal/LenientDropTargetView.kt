package com.acme.transport.lastmile.drop.proposal

import com.acme.transport.kit.text.truncate

/** How screens and exports show a lenient drop target. */
data class LenientDropTargetView(
    var codeText: String,
    var title: String,
    var floorValue: String,
    var description: String,
    var capacity: String,
    var active: String,
    var fuelLitersText: String,
    var limit: String,
    var score: String,
    var statusLabel: String,
)

fun toLenientDropTargetView(item: LenientDropTarget): LenientDropTargetView {
    return LenientDropTargetView(
        codeText = truncate(item.code, 64),
        title = item.title,
        floorValue = "%.2f".format(item.floorValue),
        description = item.description,
        capacity = item.capacity.toString(),
        active = "${item.active}",
        fuelLitersText = "%.2f".format(item.fuelLiters),
        limit = item.limit.toString(),
        score = "%.2f".format(item.score),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLenientDropTargetViews(items: MutableList<LenientDropTarget>): MutableList<LenientDropTargetView> {
    val views = mutableListOf<LenientDropTargetView>()
    for (item in items) {
        views.add(toLenientDropTargetView(item))
    }
    return views
}

fun sumLenientDropTargetView(view: LenientDropTargetView): Double {
    val parsedFloorValue = view.floorValue.toDouble()
    val parsedCapacity = view.capacity.toInt()
    val parsedFuelLiters = view.fuelLitersText.toDouble()
    return parsedFloorValue + parsedCapacity.toDouble() + parsedFuelLiters
}
