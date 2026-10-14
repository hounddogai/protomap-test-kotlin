package com.acme.transport.fleetops.tire.draft

import com.acme.transport.kit.text.truncate

/** How screens and exports show a default tire estimate. */
data class DefaultTireEstimateView(
    var codeText: String,
    var name: String,
    var capacityText: String,
    var threshold: String,
    var reviewer: String,
    var windowDays: String,
    var statusLabel: String,
)

fun toDefaultTireEstimateView(item: DefaultTireEstimate): DefaultTireEstimateView {
    return DefaultTireEstimateView(
        codeText = truncate(item.code, 120),
        name = item.name,
        capacityText = item.capacity.toString(),
        threshold = "%.2f".format(item.threshold),
        reviewer = item.reviewer,
        windowDays = item.windowDays.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDefaultTireEstimateViews(items: MutableList<DefaultTireEstimate>): MutableList<DefaultTireEstimateView> {
    val views = mutableListOf<DefaultTireEstimateView>()
    for (item in items) {
        views.add(toDefaultTireEstimateView(item))
    }
    return views
}

fun sumDefaultTireEstimateView(view: DefaultTireEstimateView): Double {
    val parsedCapacity = view.capacityText.toInt()
    val parsedThreshold = view.threshold.toDouble()
    val parsedWindowDays = view.windowDays.toInt()
    return parsedCapacity.toDouble() + parsedThreshold + parsedWindowDays.toDouble()
}
