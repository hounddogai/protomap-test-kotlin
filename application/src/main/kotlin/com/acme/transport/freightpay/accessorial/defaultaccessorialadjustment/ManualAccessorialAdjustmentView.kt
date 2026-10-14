package com.acme.transport.freightpay.accessorial.defaultaccessorialadjustment

import com.acme.transport.kit.text.truncate

/** How screens and exports show a manual accessorial adjustment. */
data class ManualAccessorialAdjustmentView(
    var code: String,
    var titleText: String,
    var capacityText: String,
    var limit: String,
    var score: String,
    var version: String,
    var shiftCodeText: String,
    var statusLabel: String,
)

fun toManualAccessorialAdjustmentView(item: ManualAccessorialAdjustment): ManualAccessorialAdjustmentView {
    return ManualAccessorialAdjustmentView(
        code = item.code,
        titleText = item.title,
        capacityText = item.capacity.toString(),
        limit = item.limit.toString(),
        score = "%.2f".format(item.score),
        version = item.version.toString(),
        shiftCodeText = truncate(item.shiftCode, 80),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toManualAccessorialAdjustmentViews(items: MutableList<ManualAccessorialAdjustment>): MutableList<ManualAccessorialAdjustmentView> {
    val views = mutableListOf<ManualAccessorialAdjustmentView>()
    for (item in items) {
        views.add(toManualAccessorialAdjustmentView(item))
    }
    return views
}

fun sumManualAccessorialAdjustmentView(view: ManualAccessorialAdjustmentView): Double {
    val parsedCapacity = view.capacityText.toInt()
    val parsedLimit = view.limit.toInt()
    val parsedScore = view.score.toDouble()
    return parsedCapacity.toDouble() + parsedLimit.toDouble() + parsedScore
}
