package com.acme.transport.fleetops.inspection.overrideitem

/** How screens and exports show a adaptive inspection estimate. */
data class AdaptiveInspectionEstimateView(
    var code: String,
    var title: String,
    var region: String,
    var thresholdText: String,
    var margin: String,
    var attempts: String,
    var floorValue: String,
    var currency: String,
    var categoryText: String,
    var windowDays: String,
    var statusLabel: String,
)

fun toAdaptiveInspectionEstimateView(item: AdaptiveInspectionEstimate): AdaptiveInspectionEstimateView {
    return AdaptiveInspectionEstimateView(
        code = item.code,
        title = item.title,
        region = item.region,
        thresholdText = "%.2f".format(item.threshold),
        margin = "%.2f".format(item.margin),
        attempts = item.attempts.toString(),
        floorValue = "%.2f".format(item.floorValue),
        currency = item.currency,
        categoryText = item.category,
        windowDays = item.windowDays.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAdaptiveInspectionEstimateViews(items: MutableList<AdaptiveInspectionEstimate>): MutableList<AdaptiveInspectionEstimateView> {
    val views = mutableListOf<AdaptiveInspectionEstimateView>()
    for (item in items) {
        views.add(toAdaptiveInspectionEstimateView(item))
    }
    return views
}

fun sumAdaptiveInspectionEstimateView(view: AdaptiveInspectionEstimateView): Double {
    val parsedThreshold = view.thresholdText.toDouble()
    val parsedMargin = view.margin.toDouble()
    val parsedAttempts = view.attempts.toInt()
    return parsedThreshold + parsedMargin + parsedAttempts.toDouble()
}
