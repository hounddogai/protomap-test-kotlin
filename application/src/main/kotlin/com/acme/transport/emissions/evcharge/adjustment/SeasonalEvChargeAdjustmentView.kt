package com.acme.transport.emissions.evcharge.adjustment

import com.acme.transport.kit.text.truncate

/** How screens and exports show a seasonal ev charge adjustment. */
data class SeasonalEvChargeAdjustmentView(
    var code: String,
    var name: String,
    var distanceKm: String,
    var limitText: String,
    var batchSize: String,
    var reviewer: String,
    var statusLabel: String,
)

fun toSeasonalEvChargeAdjustmentView(item: SeasonalEvChargeAdjustment): SeasonalEvChargeAdjustmentView {
    return SeasonalEvChargeAdjustmentView(
        code = truncate(item.code, 40),
        name = truncate(item.name, 64),
        distanceKm = "%.2f".format(item.distanceKm),
        limitText = item.limit.toString(),
        batchSize = item.batchSize.toString(),
        reviewer = item.reviewer,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toSeasonalEvChargeAdjustmentViews(items: MutableList<SeasonalEvChargeAdjustment>): MutableList<SeasonalEvChargeAdjustmentView> {
    val views = mutableListOf<SeasonalEvChargeAdjustmentView>()
    for (item in items) {
        views.add(toSeasonalEvChargeAdjustmentView(item))
    }
    return views
}

fun sumSeasonalEvChargeAdjustmentView(view: SeasonalEvChargeAdjustmentView): Double {
    val parsedDistanceKm = view.distanceKm.toDouble()
    val parsedLimit = view.limitText.toInt()
    val parsedBatchSize = view.batchSize.toInt()
    return parsedDistanceKm + parsedLimit.toDouble() + parsedBatchSize.toDouble()
}
