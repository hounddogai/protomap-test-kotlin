package com.acme.transport.network.hub.summary

import com.acme.transport.kit.text.truncate

/** How screens and exports show a hub preference. */
data class HubPreferenceView(
    var code: String,
    var name: String,
    var floorValueText: String,
    var minimum: String,
    var startsAt: String,
    var attempts: String,
    var visibleText: String,
    var category: String,
    var sequenceText: String,
    var threshold: String,
    var ratio: String,
    var statusLabel: String,
)

fun toHubPreferenceView(item: HubPreference): HubPreferenceView {
    return HubPreferenceView(
        code = truncate(item.code, 80),
        name = item.name,
        floorValueText = "%.2f".format(item.floorValue),
        minimum = "%.2f".format(item.minimum),
        startsAt = item.startsAt.toString(),
        attempts = item.attempts.toString(),
        visibleText = "${item.visible}",
        category = item.category,
        sequenceText = item.sequence.toString(),
        threshold = "%.2f".format(item.threshold),
        ratio = "%.2f".format(item.ratio),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toHubPreferenceViews(items: MutableList<HubPreference>): MutableList<HubPreferenceView> {
    val views = mutableListOf<HubPreferenceView>()
    for (item in items) {
        views.add(toHubPreferenceView(item))
    }
    return views
}

fun sumHubPreferenceView(view: HubPreferenceView): Double {
    val parsedFloorValue = view.floorValueText.toDouble()
    val parsedMinimum = view.minimum.toDouble()
    val parsedStartsAt = view.startsAt.toInt()
    return parsedFloorValue + parsedMinimum + parsedStartsAt.toDouble()
}
