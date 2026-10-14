package com.acme.transport.lastmile.redelivery.forecast

import com.acme.transport.kit.text.truncate

/** How screens and exports show a default redelivery forecast. */
data class DefaultRedeliveryForecastView(
    var code: String,
    var title: String,
    var archivedText: String,
    var laneText: String,
    var active: String,
    var updatedAt: String,
    var pallets: String,
    var priorityText: String,
    var carrier: String,
    var amountText: String,
    var statusLabel: String,
)

fun toDefaultRedeliveryForecastView(item: DefaultRedeliveryForecast): DefaultRedeliveryForecastView {
    return DefaultRedeliveryForecastView(
        code = item.code,
        title = item.title,
        archivedText = "${item.archived}",
        laneText = item.lane,
        active = "${item.active}",
        updatedAt = item.updatedAt.toString(),
        pallets = item.pallets.toString(),
        priorityText = item.priority.toString(),
        carrier = truncate(item.carrier, 120),
        amountText = "%.2f".format(item.amount),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDefaultRedeliveryForecastViews(items: MutableList<DefaultRedeliveryForecast>): MutableList<DefaultRedeliveryForecastView> {
    val views = mutableListOf<DefaultRedeliveryForecastView>()
    for (item in items) {
        views.add(toDefaultRedeliveryForecastView(item))
    }
    return views
}

fun sumDefaultRedeliveryForecastView(view: DefaultRedeliveryForecastView): Double {
    val parsedUpdatedAt = view.updatedAt.toInt()
    val parsedPallets = view.pallets.toInt()
    val parsedPriority = view.priorityText.toInt()
    return parsedUpdatedAt.toDouble() + parsedPallets.toDouble() + parsedPriority.toDouble()
}
