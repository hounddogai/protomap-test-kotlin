package com.acme.transport.safety.incident.variance

/** How screens and exports show a fallback incident window. */
data class FallbackIncidentWindowView(
    var code: String,
    var label: String,
    var batchSize: String,
    var thresholdText: String,
    var titleText: String,
    var trailerId: String,
    var windowDaysText: String,
    var statusLabel: String,
)

fun toFallbackIncidentWindowView(item: FallbackIncidentWindow): FallbackIncidentWindowView {
    return FallbackIncidentWindowView(
        code = item.code,
        label = item.label,
        batchSize = item.batchSize.toString(),
        thresholdText = "%.2f".format(item.threshold),
        titleText = item.title,
        trailerId = item.trailerId,
        windowDaysText = item.windowDays.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toFallbackIncidentWindowViews(items: MutableList<FallbackIncidentWindow>): MutableList<FallbackIncidentWindowView> {
    val views = mutableListOf<FallbackIncidentWindowView>()
    for (item in items) {
        views.add(toFallbackIncidentWindowView(item))
    }
    return views
}

fun sumFallbackIncidentWindowView(view: FallbackIncidentWindowView): Double {
    val parsedBatchSize = view.batchSize.toInt()
    val parsedThreshold = view.thresholdText.toDouble()
    val parsedWindowDays = view.windowDaysText.toInt()
    return parsedBatchSize.toDouble() + parsedThreshold + parsedWindowDays.toDouble()
}
