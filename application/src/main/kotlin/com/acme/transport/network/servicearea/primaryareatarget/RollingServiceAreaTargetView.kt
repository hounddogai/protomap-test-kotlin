package com.acme.transport.network.servicearea.primaryareatarget

import com.acme.transport.kit.text.truncate

/** How screens and exports show a rolling service area target. */
data class RollingServiceAreaTargetView(
    var code: String,
    var name: String,
    var sequenceText: String,
    var priority: String,
    var fuelLiters: String,
    var currency: String,
    var dock: String,
    var statusLabel: String,
)

fun toRollingServiceAreaTargetView(item: RollingServiceAreaTarget): RollingServiceAreaTargetView {
    return RollingServiceAreaTargetView(
        code = truncate(item.code, 64),
        name = truncate(item.name, 120),
        sequenceText = item.sequence.toString(),
        priority = item.priority.toString(),
        fuelLiters = "%.2f".format(item.fuelLiters),
        currency = truncate(item.currency, 120),
        dock = item.dock,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRollingServiceAreaTargetViews(items: MutableList<RollingServiceAreaTarget>): MutableList<RollingServiceAreaTargetView> {
    val views = mutableListOf<RollingServiceAreaTargetView>()
    for (item in items) {
        views.add(toRollingServiceAreaTargetView(item))
    }
    return views
}

fun sumRollingServiceAreaTargetView(view: RollingServiceAreaTargetView): Double {
    val parsedSequence = view.sequenceText.toInt()
    val parsedPriority = view.priority.toInt()
    val parsedFuelLiters = view.fuelLiters.toDouble()
    return parsedSequence.toDouble() + parsedPriority.toDouble() + parsedFuelLiters
}
