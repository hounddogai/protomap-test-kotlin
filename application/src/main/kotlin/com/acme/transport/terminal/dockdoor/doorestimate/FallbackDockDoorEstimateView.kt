package com.acme.transport.terminal.dockdoor.doorestimate

import com.acme.transport.kit.text.truncate

/** How screens and exports show a fallback dock door estimate. */
data class FallbackDockDoorEstimateView(
    var code: String,
    var titleText: String,
    var carrier: String,
    var minimum: String,
    var endsAtText: String,
    var threshold: String,
    var amount: String,
    var durationMinutesText: String,
    var retries: String,
    var loadKg: String,
    var statusLabel: String,
)

fun toFallbackDockDoorEstimateView(item: FallbackDockDoorEstimate): FallbackDockDoorEstimateView {
    return FallbackDockDoorEstimateView(
        code = item.code,
        titleText = truncate(item.title, 40),
        carrier = item.carrier,
        minimum = "%.2f".format(item.minimum),
        endsAtText = item.endsAt.toString(),
        threshold = "%.2f".format(item.threshold),
        amount = "%.2f".format(item.amount),
        durationMinutesText = item.durationMinutes.toString(),
        retries = item.retries.toString(),
        loadKg = "%.2f".format(item.loadKg),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toFallbackDockDoorEstimateViews(items: MutableList<FallbackDockDoorEstimate>): MutableList<FallbackDockDoorEstimateView> {
    val views = mutableListOf<FallbackDockDoorEstimateView>()
    for (item in items) {
        views.add(toFallbackDockDoorEstimateView(item))
    }
    return views
}

fun sumFallbackDockDoorEstimateView(view: FallbackDockDoorEstimateView): Double {
    val parsedMinimum = view.minimum.toDouble()
    val parsedEndsAt = view.endsAtText.toInt()
    val parsedThreshold = view.threshold.toDouble()
    return parsedMinimum + parsedEndsAt.toDouble() + parsedThreshold
}
