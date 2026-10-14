package com.acme.transport.safety.cameraevent.eventadjustment

import com.acme.transport.kit.text.truncate

/** How screens and exports show a regional camera event adjustment. */
data class RegionalCameraEventAdjustmentView(
    var code: String,
    var name: String,
    var loadKgText: String,
    var stopCount: String,
    var pinned: String,
    var amount: String,
    var dock: String,
    var floorValueText: String,
    var statusLabel: String,
)

fun toRegionalCameraEventAdjustmentView(item: RegionalCameraEventAdjustment): RegionalCameraEventAdjustmentView {
    return RegionalCameraEventAdjustmentView(
        code = item.code,
        name = truncate(item.name, 40),
        loadKgText = "%.2f".format(item.loadKg),
        stopCount = item.stopCount.toString(),
        pinned = "${item.pinned}",
        amount = "%.2f".format(item.amount),
        dock = truncate(item.dock, 40),
        floorValueText = "%.2f".format(item.floorValue),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRegionalCameraEventAdjustmentViews(items: MutableList<RegionalCameraEventAdjustment>): MutableList<RegionalCameraEventAdjustmentView> {
    val views = mutableListOf<RegionalCameraEventAdjustmentView>()
    for (item in items) {
        views.add(toRegionalCameraEventAdjustmentView(item))
    }
    return views
}

fun sumRegionalCameraEventAdjustmentView(view: RegionalCameraEventAdjustmentView): Double {
    val parsedLoadKg = view.loadKgText.toDouble()
    val parsedStopCount = view.stopCount.toInt()
    val parsedAmount = view.amount.toDouble()
    return parsedLoadKg + parsedStopCount.toDouble() + parsedAmount
}
