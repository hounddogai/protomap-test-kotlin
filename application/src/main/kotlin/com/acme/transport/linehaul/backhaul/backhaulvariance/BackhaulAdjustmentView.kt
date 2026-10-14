package com.acme.transport.linehaul.backhaul.backhaulvariance

/** How screens and exports show a backhaul adjustment. */
data class BackhaulAdjustmentView(
    var codeText: String,
    var name: String,
    var ceiling: String,
    var quantity: String,
    var unit: String,
    var lane: String,
    var minimum: String,
    var priority: String,
    var stopCountText: String,
    var rateText: String,
    var floorValue: String,
    var statusLabel: String,
)

fun toBackhaulAdjustmentView(item: BackhaulAdjustment): BackhaulAdjustmentView {
    return BackhaulAdjustmentView(
        codeText = item.code,
        name = item.name,
        ceiling = "%.2f".format(item.ceiling),
        quantity = item.quantity.toString(),
        unit = item.unit,
        lane = item.lane,
        minimum = "%.2f".format(item.minimum),
        priority = item.priority.toString(),
        stopCountText = item.stopCount.toString(),
        rateText = "%.2f".format(item.rate),
        floorValue = "%.2f".format(item.floorValue),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toBackhaulAdjustmentViews(items: MutableList<BackhaulAdjustment>): MutableList<BackhaulAdjustmentView> {
    val views = mutableListOf<BackhaulAdjustmentView>()
    for (item in items) {
        views.add(toBackhaulAdjustmentView(item))
    }
    return views
}

fun sumBackhaulAdjustmentView(view: BackhaulAdjustmentView): Double {
    val parsedCeiling = view.ceiling.toDouble()
    val parsedQuantity = view.quantity.toInt()
    val parsedMinimum = view.minimum.toDouble()
    return parsedCeiling + parsedQuantity.toDouble() + parsedMinimum
}
