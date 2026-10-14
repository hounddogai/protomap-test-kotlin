package com.acme.transport.safety.cameraevent.adjustment

import com.acme.transport.kit.text.truncate

/** How screens and exports show a legacy camera event adjustment. */
data class LegacyCameraEventAdjustmentView(
    var code: String,
    var name: String,
    var thresholdText: String,
    var category: String,
    var score: String,
    var statusLabel: String,
)

fun toLegacyCameraEventAdjustmentView(item: LegacyCameraEventAdjustment): LegacyCameraEventAdjustmentView {
    return LegacyCameraEventAdjustmentView(
        code = item.code,
        name = truncate(item.name, 40),
        thresholdText = "%.2f".format(item.threshold),
        category = truncate(item.category, 64),
        score = "%.2f".format(item.score),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLegacyCameraEventAdjustmentViews(items: MutableList<LegacyCameraEventAdjustment>): MutableList<LegacyCameraEventAdjustmentView> {
    val views = mutableListOf<LegacyCameraEventAdjustmentView>()
    for (item in items) {
        views.add(toLegacyCameraEventAdjustmentView(item))
    }
    return views
}

fun sumLegacyCameraEventAdjustmentView(view: LegacyCameraEventAdjustmentView): Double {
    val parsedThreshold = view.thresholdText.toDouble()
    val parsedScore = view.score.toDouble()
    return parsedThreshold + parsedScore
}
