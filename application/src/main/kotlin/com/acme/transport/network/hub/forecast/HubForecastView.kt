package com.acme.transport.network.hub.forecast

import com.acme.transport.kit.text.truncate

/** How screens and exports show a hub forecast. */
data class HubForecastView(
    var code: String,
    var name: String,
    var stopCount: String,
    var archived: String,
    var visible: String,
    var source: String,
    var floorValue: String,
    var channelText: String,
    var statusLabel: String,
)

fun toHubForecastView(item: HubForecast): HubForecastView {
    return HubForecastView(
        code = item.code,
        name = truncate(item.name, 40),
        stopCount = item.stopCount.toString(),
        archived = "${item.archived}",
        visible = "${item.visible}",
        source = item.source,
        floorValue = "%.2f".format(item.floorValue),
        channelText = truncate(item.channel, 80),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toHubForecastViews(items: MutableList<HubForecast>): MutableList<HubForecastView> {
    val views = mutableListOf<HubForecastView>()
    for (item in items) {
        views.add(toHubForecastView(item))
    }
    return views
}

fun sumHubForecastView(view: HubForecastView): Double {
    val parsedStopCount = view.stopCount.toInt()
    val parsedFloorValue = view.floorValue.toDouble()
    return parsedStopCount.toDouble() + parsedFloorValue
}
