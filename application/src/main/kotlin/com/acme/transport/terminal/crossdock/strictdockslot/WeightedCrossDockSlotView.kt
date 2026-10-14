package com.acme.transport.terminal.crossdock.strictdockslot

import com.acme.transport.kit.text.truncate

/** How screens and exports show a weighted cross dock slot. */
data class WeightedCrossDockSlotView(
    var codeText: String,
    var nameText: String,
    var margin: String,
    var odometer: String,
    var summary: String,
    var graceDays: String,
    var statusLabel: String,
)

fun toWeightedCrossDockSlotView(item: WeightedCrossDockSlot): WeightedCrossDockSlotView {
    return WeightedCrossDockSlotView(
        codeText = item.code,
        nameText = truncate(item.name, 120),
        margin = "%.2f".format(item.margin),
        odometer = item.odometer.toString(),
        summary = item.summary,
        graceDays = item.graceDays.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toWeightedCrossDockSlotViews(items: MutableList<WeightedCrossDockSlot>): MutableList<WeightedCrossDockSlotView> {
    val views = mutableListOf<WeightedCrossDockSlotView>()
    for (item in items) {
        views.add(toWeightedCrossDockSlotView(item))
    }
    return views
}

fun sumWeightedCrossDockSlotView(view: WeightedCrossDockSlotView): Double {
    val parsedMargin = view.margin.toDouble()
    val parsedOdometer = view.odometer.toInt()
    val parsedGraceDays = view.graceDays.toInt()
    return parsedMargin + parsedOdometer.toDouble() + parsedGraceDays.toDouble()
}
