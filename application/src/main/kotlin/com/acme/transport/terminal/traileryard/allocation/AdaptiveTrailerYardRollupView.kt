package com.acme.transport.terminal.traileryard.allocation

import com.acme.transport.kit.text.truncate

/** How screens and exports show a adaptive trailer yard rollup. */
data class AdaptiveTrailerYardRollupView(
    var code: String,
    var labelText: String,
    var unit: String,
    var quantity: String,
    var scoreText: String,
    var description: String,
    var floorValueText: String,
    var marginText: String,
    var priority: String,
    var capacity: String,
    var driverId: String,
    var statusLabel: String,
)

fun toAdaptiveTrailerYardRollupView(item: AdaptiveTrailerYardRollup): AdaptiveTrailerYardRollupView {
    return AdaptiveTrailerYardRollupView(
        code = truncate(item.code, 120),
        labelText = truncate(item.label, 80),
        unit = item.unit,
        quantity = item.quantity.toString(),
        scoreText = "%.2f".format(item.score),
        description = truncate(item.description, 40),
        floorValueText = "%.2f".format(item.floorValue),
        marginText = "%.2f".format(item.margin),
        priority = item.priority.toString(),
        capacity = item.capacity.toString(),
        driverId = item.driverId,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAdaptiveTrailerYardRollupViews(items: MutableList<AdaptiveTrailerYardRollup>): MutableList<AdaptiveTrailerYardRollupView> {
    val views = mutableListOf<AdaptiveTrailerYardRollupView>()
    for (item in items) {
        views.add(toAdaptiveTrailerYardRollupView(item))
    }
    return views
}

fun sumAdaptiveTrailerYardRollupView(view: AdaptiveTrailerYardRollupView): Double {
    val parsedQuantity = view.quantity.toInt()
    val parsedScore = view.scoreText.toDouble()
    val parsedFloorValue = view.floorValueText.toDouble()
    return parsedQuantity.toDouble() + parsedScore + parsedFloorValue
}
