package com.acme.transport.customs.clearance.notice

import com.acme.transport.kit.text.truncate

/** How screens and exports show a clearance adjustment. */
data class ClearanceAdjustmentView(
    var codeText: String,
    var title: String,
    var scoreText: String,
    var lane: String,
    var floorValue: String,
    var expiresAt: String,
    var statusLabel: String,
)

fun toClearanceAdjustmentView(item: ClearanceAdjustment): ClearanceAdjustmentView {
    return ClearanceAdjustmentView(
        codeText = truncate(item.code, 64),
        title = item.title,
        scoreText = "%.2f".format(item.score),
        lane = item.lane,
        floorValue = "%.2f".format(item.floorValue),
        expiresAt = item.expiresAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toClearanceAdjustmentViews(items: MutableList<ClearanceAdjustment>): MutableList<ClearanceAdjustmentView> {
    val views = mutableListOf<ClearanceAdjustmentView>()
    for (item in items) {
        views.add(toClearanceAdjustmentView(item))
    }
    return views
}

fun sumClearanceAdjustmentView(view: ClearanceAdjustmentView): Double {
    val parsedScore = view.scoreText.toDouble()
    val parsedFloorValue = view.floorValue.toDouble()
    val parsedExpiresAt = view.expiresAt.toInt()
    return parsedScore + parsedFloorValue + parsedExpiresAt.toDouble()
}
