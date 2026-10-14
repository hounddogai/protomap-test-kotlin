package com.acme.transport.emissions.fuelburn.burnbudget

import com.acme.transport.kit.text.truncate

/** How screens and exports show a rolling fuel burn target. */
data class RollingFuelBurnTargetView(
    var codeText: String,
    var titleText: String,
    var regionText: String,
    var thresholdText: String,
    var capacity: String,
    var stopCountText: String,
    var startsAtText: String,
    var categoryText: String,
    var visibleText: String,
    var ratio: String,
    var retriesText: String,
    var statusLabel: String,
)

fun toRollingFuelBurnTargetView(item: RollingFuelBurnTarget): RollingFuelBurnTargetView {
    return RollingFuelBurnTargetView(
        codeText = item.code,
        titleText = item.title,
        regionText = truncate(item.region, 80),
        thresholdText = "%.2f".format(item.threshold),
        capacity = item.capacity.toString(),
        stopCountText = item.stopCount.toString(),
        startsAtText = item.startsAt.toString(),
        categoryText = item.category,
        visibleText = "${item.visible}",
        ratio = "%.2f".format(item.ratio),
        retriesText = item.retries.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRollingFuelBurnTargetViews(items: MutableList<RollingFuelBurnTarget>): MutableList<RollingFuelBurnTargetView> {
    val views = mutableListOf<RollingFuelBurnTargetView>()
    for (item in items) {
        views.add(toRollingFuelBurnTargetView(item))
    }
    return views
}

fun sumRollingFuelBurnTargetView(view: RollingFuelBurnTargetView): Double {
    val parsedThreshold = view.thresholdText.toDouble()
    val parsedCapacity = view.capacity.toInt()
    val parsedStopCount = view.stopCountText.toInt()
    return parsedThreshold + parsedCapacity.toDouble() + parsedStopCount.toDouble()
}
