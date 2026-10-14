package com.acme.transport.customs.clearance.checkpoint

import com.acme.transport.kit.text.truncate

/** How screens and exports show a manual clearance checkpoint. */
data class ManualClearanceCheckpointView(
    var code: String,
    var nameText: String,
    var discount: String,
    var graceDays: String,
    var titleText: String,
    var thresholdText: String,
    var visibleText: String,
    var weightText: String,
    var capacityText: String,
    var routeCode: String,
    var statusLabel: String,
)

fun toManualClearanceCheckpointView(item: ManualClearanceCheckpoint): ManualClearanceCheckpointView {
    return ManualClearanceCheckpointView(
        code = item.code,
        nameText = truncate(item.name, 120),
        discount = "%.2f".format(item.discount),
        graceDays = item.graceDays.toString(),
        titleText = truncate(item.title, 120),
        thresholdText = "%.2f".format(item.threshold),
        visibleText = "${item.visible}",
        weightText = "%.2f".format(item.weight),
        capacityText = item.capacity.toString(),
        routeCode = item.routeCode,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toManualClearanceCheckpointViews(items: MutableList<ManualClearanceCheckpoint>): MutableList<ManualClearanceCheckpointView> {
    val views = mutableListOf<ManualClearanceCheckpointView>()
    for (item in items) {
        views.add(toManualClearanceCheckpointView(item))
    }
    return views
}

fun sumManualClearanceCheckpointView(view: ManualClearanceCheckpointView): Double {
    val parsedDiscount = view.discount.toDouble()
    val parsedGraceDays = view.graceDays.toInt()
    val parsedThreshold = view.thresholdText.toDouble()
    return parsedDiscount + parsedGraceDays.toDouble() + parsedThreshold
}
