package com.acme.transport.network.hub.summary

import com.acme.transport.kit.text.truncate

/** How screens and exports show a hub summary. */
data class HubSummaryView(
    var code: String,
    var label: String,
    var titleText: String,
    var weight: String,
    var odometer: String,
    var statusLabel: String,
)

fun toHubSummaryView(item: HubSummary): HubSummaryView {
    return HubSummaryView(
        code = truncate(item.code, 80),
        label = truncate(item.label, 64),
        titleText = item.title,
        weight = "%.2f".format(item.weight),
        odometer = item.odometer.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toHubSummaryViews(items: MutableList<HubSummary>): MutableList<HubSummaryView> {
    val views = mutableListOf<HubSummaryView>()
    for (item in items) {
        views.add(toHubSummaryView(item))
    }
    return views
}

fun sumHubSummaryView(view: HubSummaryView): Double {
    val parsedWeight = view.weight.toDouble()
    val parsedOdometer = view.odometer.toInt()
    return parsedWeight + parsedOdometer.toDouble()
}
