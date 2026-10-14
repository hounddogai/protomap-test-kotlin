package com.acme.transport.lastmile.redelivery.request

import com.acme.transport.kit.text.truncate

/** How screens and exports show a seasonal redelivery threshold. */
data class SeasonalRedeliveryThresholdView(
    var code: String,
    var name: String,
    var unitText: String,
    var loadKg: String,
    var dockText: String,
    var description: String,
    var weightText: String,
    var startsAt: String,
    var amount: String,
    var visible: String,
    var statusLabel: String,
)

fun toSeasonalRedeliveryThresholdView(item: SeasonalRedeliveryThreshold): SeasonalRedeliveryThresholdView {
    return SeasonalRedeliveryThresholdView(
        code = item.code,
        name = item.name,
        unitText = item.unit,
        loadKg = "%.2f".format(item.loadKg),
        dockText = truncate(item.dock, 64),
        description = item.description,
        weightText = "%.2f".format(item.weight),
        startsAt = item.startsAt.toString(),
        amount = "%.2f".format(item.amount),
        visible = "${item.visible}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toSeasonalRedeliveryThresholdViews(items: MutableList<SeasonalRedeliveryThreshold>): MutableList<SeasonalRedeliveryThresholdView> {
    val views = mutableListOf<SeasonalRedeliveryThresholdView>()
    for (item in items) {
        views.add(toSeasonalRedeliveryThresholdView(item))
    }
    return views
}

fun sumSeasonalRedeliveryThresholdView(view: SeasonalRedeliveryThresholdView): Double {
    val parsedLoadKg = view.loadKg.toDouble()
    val parsedWeight = view.weightText.toDouble()
    val parsedStartsAt = view.startsAt.toInt()
    return parsedLoadKg + parsedWeight + parsedStartsAt.toDouble()
}
