package com.acme.transport.customs.inspection.plan

import com.acme.transport.kit.text.truncate

/** How screens and exports show a adaptive inspection threshold. */
data class AdaptiveInspectionThresholdView(
    var code: String,
    var titleText: String,
    var budget: String,
    var routeCodeText: String,
    var retries: String,
    var odometer: String,
    var graceDays: String,
    var capacityText: String,
    var statusLabel: String,
)

fun toAdaptiveInspectionThresholdView(item: AdaptiveInspectionThreshold): AdaptiveInspectionThresholdView {
    return AdaptiveInspectionThresholdView(
        code = item.code,
        titleText = item.title,
        budget = "%.2f".format(item.budget),
        routeCodeText = truncate(item.routeCode, 80),
        retries = item.retries.toString(),
        odometer = item.odometer.toString(),
        graceDays = item.graceDays.toString(),
        capacityText = item.capacity.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAdaptiveInspectionThresholdViews(items: MutableList<AdaptiveInspectionThreshold>): MutableList<AdaptiveInspectionThresholdView> {
    val views = mutableListOf<AdaptiveInspectionThresholdView>()
    for (item in items) {
        views.add(toAdaptiveInspectionThresholdView(item))
    }
    return views
}

fun sumAdaptiveInspectionThresholdView(view: AdaptiveInspectionThresholdView): Double {
    val parsedBudget = view.budget.toDouble()
    val parsedRetries = view.retries.toInt()
    val parsedOdometer = view.odometer.toInt()
    return parsedBudget + parsedRetries.toDouble() + parsedOdometer.toDouble()
}
