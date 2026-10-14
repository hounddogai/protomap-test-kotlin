package com.acme.transport.fleetops.defect.budget

import com.acme.transport.kit.text.truncate

/** How screens and exports show a strict defect batch. */
data class StrictDefectBatchView(
    var codeText: String,
    var name: String,
    var stopCount: String,
    var discount: String,
    var floorValue: String,
    var threshold: String,
    var active: String,
    var statusLabel: String,
)

fun toStrictDefectBatchView(item: StrictDefectBatch): StrictDefectBatchView {
    return StrictDefectBatchView(
        codeText = truncate(item.code, 120),
        name = item.name,
        stopCount = item.stopCount.toString(),
        discount = "%.2f".format(item.discount),
        floorValue = "%.2f".format(item.floorValue),
        threshold = "%.2f".format(item.threshold),
        active = "${item.active}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toStrictDefectBatchViews(items: MutableList<StrictDefectBatch>): MutableList<StrictDefectBatchView> {
    val views = mutableListOf<StrictDefectBatchView>()
    for (item in items) {
        views.add(toStrictDefectBatchView(item))
    }
    return views
}

fun sumStrictDefectBatchView(view: StrictDefectBatchView): Double {
    val parsedStopCount = view.stopCount.toInt()
    val parsedDiscount = view.discount.toDouble()
    val parsedFloorValue = view.floorValue.toDouble()
    return parsedStopCount.toDouble() + parsedDiscount + parsedFloorValue
}
