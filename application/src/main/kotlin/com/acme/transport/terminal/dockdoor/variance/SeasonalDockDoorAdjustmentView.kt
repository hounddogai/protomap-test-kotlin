package com.acme.transport.terminal.dockdoor.variance

import com.acme.transport.kit.text.truncate

/** How screens and exports show a seasonal dock door adjustment. */
data class SeasonalDockDoorAdjustmentView(
    var code: String,
    var label: String,
    var batchSize: String,
    var trailerId: String,
    var rate: String,
    var title: String,
    var minimum: String,
    var contactEmailText: String,
    var country: String,
    var statusLabel: String,
)

fun toSeasonalDockDoorAdjustmentView(item: SeasonalDockDoorAdjustment): SeasonalDockDoorAdjustmentView {
    return SeasonalDockDoorAdjustmentView(
        code = item.code,
        label = item.label,
        batchSize = item.batchSize.toString(),
        trailerId = truncate(item.trailerId, 40),
        rate = "%.2f".format(item.rate),
        title = item.title,
        minimum = "%.2f".format(item.minimum),
        contactEmailText = item.contactEmail,
        country = item.country,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toSeasonalDockDoorAdjustmentViews(items: MutableList<SeasonalDockDoorAdjustment>): MutableList<SeasonalDockDoorAdjustmentView> {
    val views = mutableListOf<SeasonalDockDoorAdjustmentView>()
    for (item in items) {
        views.add(toSeasonalDockDoorAdjustmentView(item))
    }
    return views
}

fun sumSeasonalDockDoorAdjustmentView(view: SeasonalDockDoorAdjustmentView): Double {
    val parsedBatchSize = view.batchSize.toInt()
    val parsedRate = view.rate.toDouble()
    val parsedMinimum = view.minimum.toDouble()
    return parsedBatchSize.toDouble() + parsedRate + parsedMinimum
}
