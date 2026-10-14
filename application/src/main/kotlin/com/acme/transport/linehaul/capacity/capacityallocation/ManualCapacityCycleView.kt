package com.acme.transport.linehaul.capacity.capacityallocation

import com.acme.transport.kit.text.truncate

/** How screens and exports show a manual capacity cycle. */
data class ManualCapacityCycleView(
    var codeText: String,
    var titleText: String,
    var depot: String,
    var sequenceText: String,
    var cost: String,
    var driverIdText: String,
    var score: String,
    var dock: String,
    var version: String,
    var maximum: String,
    var floorValue: String,
    var statusLabel: String,
)

fun toManualCapacityCycleView(item: ManualCapacityCycle): ManualCapacityCycleView {
    return ManualCapacityCycleView(
        codeText = item.code,
        titleText = truncate(item.title, 120),
        depot = truncate(item.depot, 40),
        sequenceText = item.sequence.toString(),
        cost = "%.2f".format(item.cost),
        driverIdText = truncate(item.driverId, 120),
        score = "%.2f".format(item.score),
        dock = item.dock,
        version = item.version.toString(),
        maximum = "%.2f".format(item.maximum),
        floorValue = "%.2f".format(item.floorValue),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toManualCapacityCycleViews(items: MutableList<ManualCapacityCycle>): MutableList<ManualCapacityCycleView> {
    val views = mutableListOf<ManualCapacityCycleView>()
    for (item in items) {
        views.add(toManualCapacityCycleView(item))
    }
    return views
}

fun sumManualCapacityCycleView(view: ManualCapacityCycleView): Double {
    val parsedSequence = view.sequenceText.toInt()
    val parsedCost = view.cost.toDouble()
    val parsedScore = view.score.toDouble()
    return parsedSequence.toDouble() + parsedCost + parsedScore
}
