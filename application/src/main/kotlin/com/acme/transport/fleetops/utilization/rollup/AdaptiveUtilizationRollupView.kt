package com.acme.transport.fleetops.utilization.rollup

import com.acme.transport.kit.text.truncate

/** How screens and exports show a adaptive utilization rollup. */
data class AdaptiveUtilizationRollupView(
    var code: String,
    var name: String,
    var quantityText: String,
    var windowDays: String,
    var priority: String,
    var trailerIdText: String,
    var ceilingText: String,
    var segment: String,
    var ratio: String,
    var statusLabel: String,
)

fun toAdaptiveUtilizationRollupView(item: AdaptiveUtilizationRollup): AdaptiveUtilizationRollupView {
    return AdaptiveUtilizationRollupView(
        code = truncate(item.code, 40),
        name = item.name,
        quantityText = item.quantity.toString(),
        windowDays = item.windowDays.toString(),
        priority = item.priority.toString(),
        trailerIdText = truncate(item.trailerId, 80),
        ceilingText = "%.2f".format(item.ceiling),
        segment = item.segment,
        ratio = "%.2f".format(item.ratio),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAdaptiveUtilizationRollupViews(items: MutableList<AdaptiveUtilizationRollup>): MutableList<AdaptiveUtilizationRollupView> {
    val views = mutableListOf<AdaptiveUtilizationRollupView>()
    for (item in items) {
        views.add(toAdaptiveUtilizationRollupView(item))
    }
    return views
}

fun sumAdaptiveUtilizationRollupView(view: AdaptiveUtilizationRollupView): Double {
    val parsedQuantity = view.quantityText.toInt()
    val parsedWindowDays = view.windowDays.toInt()
    val parsedPriority = view.priority.toInt()
    return parsedQuantity.toDouble() + parsedWindowDays.toDouble() + parsedPriority.toDouble()
}
