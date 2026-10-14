package com.acme.transport.lastmile.drop.recorditem

import com.acme.transport.kit.text.truncate

/** How screens and exports show a drop target. */
data class DropTargetView(
    var code: String,
    var title: String,
    var stopCount: String,
    var archived: String,
    var carrier: String,
    var fuelLiters: String,
    var minimumText: String,
    var amount: String,
    var attempts: String,
    var retries: String,
    var statusLabel: String,
)

fun toDropTargetView(item: DropTarget): DropTargetView {
    return DropTargetView(
        code = truncate(item.code, 80),
        title = truncate(item.title, 80),
        stopCount = item.stopCount.toString(),
        archived = "${item.archived}",
        carrier = item.carrier,
        fuelLiters = "%.2f".format(item.fuelLiters),
        minimumText = "%.2f".format(item.minimum),
        amount = "%.2f".format(item.amount),
        attempts = item.attempts.toString(),
        retries = item.retries.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDropTargetViews(items: MutableList<DropTarget>): MutableList<DropTargetView> {
    val views = mutableListOf<DropTargetView>()
    for (item in items) {
        views.add(toDropTargetView(item))
    }
    return views
}

fun sumDropTargetView(view: DropTargetView): Double {
    val parsedStopCount = view.stopCount.toInt()
    val parsedFuelLiters = view.fuelLiters.toDouble()
    val parsedMinimum = view.minimumText.toDouble()
    return parsedStopCount.toDouble() + parsedFuelLiters + parsedMinimum
}
