package com.acme.transport.customs.broker.profile

import com.acme.transport.kit.text.truncate

/** How screens and exports show a regional broker checkpoint. */
data class RegionalBrokerCheckpointView(
    var code: String,
    var label: String,
    var region: String,
    var rate: String,
    var unit: String,
    var startsAtText: String,
    var routeCodeText: String,
    var statusLabel: String,
)

fun toRegionalBrokerCheckpointView(item: RegionalBrokerCheckpoint): RegionalBrokerCheckpointView {
    return RegionalBrokerCheckpointView(
        code = item.code,
        label = item.label,
        region = truncate(item.region, 40),
        rate = "%.2f".format(item.rate),
        unit = item.unit,
        startsAtText = item.startsAt.toString(),
        routeCodeText = truncate(item.routeCode, 40),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRegionalBrokerCheckpointViews(items: MutableList<RegionalBrokerCheckpoint>): MutableList<RegionalBrokerCheckpointView> {
    val views = mutableListOf<RegionalBrokerCheckpointView>()
    for (item in items) {
        views.add(toRegionalBrokerCheckpointView(item))
    }
    return views
}

fun sumRegionalBrokerCheckpointView(view: RegionalBrokerCheckpointView): Double {
    val parsedRate = view.rate.toDouble()
    val parsedStartsAt = view.startsAtText.toInt()
    return parsedRate + parsedStartsAt.toDouble()
}
