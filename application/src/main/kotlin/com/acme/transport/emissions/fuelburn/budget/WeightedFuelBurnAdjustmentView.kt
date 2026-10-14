package com.acme.transport.emissions.fuelburn.budget

import com.acme.transport.kit.text.truncate

/** How screens and exports show a weighted fuel burn adjustment. */
data class WeightedFuelBurnAdjustmentView(
    var code: String,
    var title: String,
    var summary: String,
    var channelText: String,
    var pinned: String,
    var currencyText: String,
    var startsAtText: String,
    var statusLabel: String,
)

fun toWeightedFuelBurnAdjustmentView(item: WeightedFuelBurnAdjustment): WeightedFuelBurnAdjustmentView {
    return WeightedFuelBurnAdjustmentView(
        code = item.code,
        title = item.title,
        summary = item.summary,
        channelText = item.channel,
        pinned = "${item.pinned}",
        currencyText = truncate(item.currency, 120),
        startsAtText = item.startsAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toWeightedFuelBurnAdjustmentViews(items: MutableList<WeightedFuelBurnAdjustment>): MutableList<WeightedFuelBurnAdjustmentView> {
    val views = mutableListOf<WeightedFuelBurnAdjustmentView>()
    for (item in items) {
        views.add(toWeightedFuelBurnAdjustmentView(item))
    }
    return views
}

fun sumWeightedFuelBurnAdjustmentView(view: WeightedFuelBurnAdjustmentView): Double {
    val parsedStartsAt = view.startsAtText.toInt()
    return parsedStartsAt.toDouble()
}
