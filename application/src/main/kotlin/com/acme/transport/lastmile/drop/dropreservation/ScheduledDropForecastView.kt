package com.acme.transport.lastmile.drop.dropreservation

import com.acme.transport.kit.text.truncate

/** How screens and exports show a scheduled drop forecast. */
data class ScheduledDropForecastView(
    var code: String,
    var name: String,
    var checksum: String,
    var active: String,
    var etaMinutesText: String,
    var updatedAt: String,
    var quantityText: String,
    var depot: String,
    var weightText: String,
    var lane: String,
    var statusLabel: String,
)

fun toScheduledDropForecastView(item: ScheduledDropForecast): ScheduledDropForecastView {
    return ScheduledDropForecastView(
        code = item.code,
        name = item.name,
        checksum = truncate(item.checksum, 120),
        active = "${item.active}",
        etaMinutesText = item.etaMinutes.toString(),
        updatedAt = item.updatedAt.toString(),
        quantityText = item.quantity.toString(),
        depot = item.depot,
        weightText = "%.2f".format(item.weight),
        lane = truncate(item.lane, 120),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toScheduledDropForecastViews(items: MutableList<ScheduledDropForecast>): MutableList<ScheduledDropForecastView> {
    val views = mutableListOf<ScheduledDropForecastView>()
    for (item in items) {
        views.add(toScheduledDropForecastView(item))
    }
    return views
}

fun sumScheduledDropForecastView(view: ScheduledDropForecastView): Double {
    val parsedEtaMinutes = view.etaMinutesText.toInt()
    val parsedUpdatedAt = view.updatedAt.toInt()
    val parsedQuantity = view.quantityText.toInt()
    return parsedEtaMinutes.toDouble() + parsedUpdatedAt.toDouble() + parsedQuantity.toDouble()
}
