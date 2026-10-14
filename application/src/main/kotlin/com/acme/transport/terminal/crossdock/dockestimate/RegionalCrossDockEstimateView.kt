package com.acme.transport.terminal.crossdock.dockestimate

import com.acme.transport.kit.text.truncate

/** How screens and exports show a regional cross dock estimate. */
data class RegionalCrossDockEstimateView(
    var code: String,
    var name: String,
    var phone: String,
    var rateText: String,
    var weightText: String,
    var startsAt: String,
    var maximum: String,
    var loadKg: String,
    var segmentText: String,
    var statusLabel: String,
)

fun toRegionalCrossDockEstimateView(item: RegionalCrossDockEstimate): RegionalCrossDockEstimateView {
    return RegionalCrossDockEstimateView(
        code = item.code,
        name = truncate(item.name, 64),
        phone = item.phone,
        rateText = "%.2f".format(item.rate),
        weightText = "%.2f".format(item.weight),
        startsAt = item.startsAt.toString(),
        maximum = "%.2f".format(item.maximum),
        loadKg = "%.2f".format(item.loadKg),
        segmentText = item.segment,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRegionalCrossDockEstimateViews(items: MutableList<RegionalCrossDockEstimate>): MutableList<RegionalCrossDockEstimateView> {
    val views = mutableListOf<RegionalCrossDockEstimateView>()
    for (item in items) {
        views.add(toRegionalCrossDockEstimateView(item))
    }
    return views
}

fun sumRegionalCrossDockEstimateView(view: RegionalCrossDockEstimateView): Double {
    val parsedRate = view.rateText.toDouble()
    val parsedWeight = view.weightText.toDouble()
    val parsedStartsAt = view.startsAt.toInt()
    return parsedRate + parsedWeight + parsedStartsAt.toDouble()
}
