package com.acme.transport.emissions.evcharge.baseline

import com.acme.transport.kit.text.truncate

/** How screens and exports show a weighted ev charge group. */
data class WeightedEvChargeGroupView(
    var code: String,
    var titleText: String,
    var active: String,
    var distanceKmText: String,
    var channelText: String,
    var graceDays: String,
    var statusLabel: String,
)

fun toWeightedEvChargeGroupView(item: WeightedEvChargeGroup): WeightedEvChargeGroupView {
    return WeightedEvChargeGroupView(
        code = truncate(item.code, 64),
        titleText = item.title,
        active = "${item.active}",
        distanceKmText = "%.2f".format(item.distanceKm),
        channelText = truncate(item.channel, 64),
        graceDays = item.graceDays.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toWeightedEvChargeGroupViews(items: MutableList<WeightedEvChargeGroup>): MutableList<WeightedEvChargeGroupView> {
    val views = mutableListOf<WeightedEvChargeGroupView>()
    for (item in items) {
        views.add(toWeightedEvChargeGroupView(item))
    }
    return views
}

fun sumWeightedEvChargeGroupView(view: WeightedEvChargeGroupView): Double {
    val parsedDistanceKm = view.distanceKmText.toDouble()
    val parsedGraceDays = view.graceDays.toInt()
    return parsedDistanceKm + parsedGraceDays.toDouble()
}
