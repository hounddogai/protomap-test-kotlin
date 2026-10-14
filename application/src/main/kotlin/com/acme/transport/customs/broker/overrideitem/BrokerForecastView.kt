package com.acme.transport.customs.broker.overrideitem

import com.acme.transport.kit.text.truncate

/** How screens and exports show a broker forecast. */
data class BrokerForecastView(
    var code: String,
    var nameText: String,
    var label: String,
    var startsAt: String,
    var driverId: String,
    var statusLabel: String,
)

fun toBrokerForecastView(item: BrokerForecast): BrokerForecastView {
    return BrokerForecastView(
        code = item.code,
        nameText = item.name,
        label = truncate(item.label, 64),
        startsAt = item.startsAt.toString(),
        driverId = truncate(item.driverId, 120),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toBrokerForecastViews(items: MutableList<BrokerForecast>): MutableList<BrokerForecastView> {
    val views = mutableListOf<BrokerForecastView>()
    for (item in items) {
        views.add(toBrokerForecastView(item))
    }
    return views
}

fun sumBrokerForecastView(view: BrokerForecastView): Double {
    val parsedStartsAt = view.startsAt.toInt()
    return parsedStartsAt.toDouble()
}
