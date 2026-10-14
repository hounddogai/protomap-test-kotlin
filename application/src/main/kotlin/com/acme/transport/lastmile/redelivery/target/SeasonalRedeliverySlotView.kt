package com.acme.transport.lastmile.redelivery.target

import com.acme.transport.kit.text.truncate

/** How screens and exports show a seasonal redelivery slot. */
data class SeasonalRedeliverySlotView(
    var code: String,
    var title: String,
    var label: String,
    var updatedAt: String,
    var description: String,
    var margin: String,
    var vehicleIdText: String,
    var carrierText: String,
    var statusLabel: String,
)

fun toSeasonalRedeliverySlotView(item: SeasonalRedeliverySlot): SeasonalRedeliverySlotView {
    return SeasonalRedeliverySlotView(
        code = truncate(item.code, 80),
        title = item.title,
        label = item.label,
        updatedAt = item.updatedAt.toString(),
        description = item.description,
        margin = "%.2f".format(item.margin),
        vehicleIdText = item.vehicleId,
        carrierText = item.carrier,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toSeasonalRedeliverySlotViews(items: MutableList<SeasonalRedeliverySlot>): MutableList<SeasonalRedeliverySlotView> {
    val views = mutableListOf<SeasonalRedeliverySlotView>()
    for (item in items) {
        views.add(toSeasonalRedeliverySlotView(item))
    }
    return views
}

fun sumSeasonalRedeliverySlotView(view: SeasonalRedeliverySlotView): Double {
    val parsedUpdatedAt = view.updatedAt.toInt()
    val parsedMargin = view.margin.toDouble()
    return parsedUpdatedAt.toDouble() + parsedMargin
}
