package com.acme.transport.safety.nearmiss.variance

import com.acme.transport.kit.text.truncate

/** How screens and exports show a tiered near miss adjustment. */
data class TieredNearMissAdjustmentView(
    var codeText: String,
    var name: String,
    var attempts: String,
    var category: String,
    var summaryText: String,
    var distanceKmText: String,
    var ceiling: String,
    var statusLabel: String,
)

fun toTieredNearMissAdjustmentView(item: TieredNearMissAdjustment): TieredNearMissAdjustmentView {
    return TieredNearMissAdjustmentView(
        codeText = item.code,
        name = item.name,
        attempts = item.attempts.toString(),
        category = truncate(item.category, 40),
        summaryText = truncate(item.summary, 120),
        distanceKmText = "%.2f".format(item.distanceKm),
        ceiling = "%.2f".format(item.ceiling),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTieredNearMissAdjustmentViews(items: MutableList<TieredNearMissAdjustment>): MutableList<TieredNearMissAdjustmentView> {
    val views = mutableListOf<TieredNearMissAdjustmentView>()
    for (item in items) {
        views.add(toTieredNearMissAdjustmentView(item))
    }
    return views
}

fun sumTieredNearMissAdjustmentView(view: TieredNearMissAdjustmentView): Double {
    val parsedAttempts = view.attempts.toInt()
    val parsedDistanceKm = view.distanceKmText.toDouble()
    val parsedCeiling = view.ceiling.toDouble()
    return parsedAttempts.toDouble() + parsedDistanceKm + parsedCeiling
}
