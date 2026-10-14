package com.acme.transport.fleetops.utilization.overrideitem

import com.acme.transport.kit.text.truncate

/** How screens and exports show a utilization override. */
data class UtilizationOverrideView(
    var code: String,
    var title: String,
    var category: String,
    var segmentText: String,
    var stopCount: String,
    var windowDaysText: String,
    var currency: String,
    var statusLabel: String,
)

fun toUtilizationOverrideView(item: UtilizationOverride): UtilizationOverrideView {
    return UtilizationOverrideView(
        code = item.code,
        title = item.title,
        category = truncate(item.category, 40),
        segmentText = item.segment,
        stopCount = item.stopCount.toString(),
        windowDaysText = item.windowDays.toString(),
        currency = truncate(item.currency, 80),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toUtilizationOverrideViews(items: MutableList<UtilizationOverride>): MutableList<UtilizationOverrideView> {
    val views = mutableListOf<UtilizationOverrideView>()
    for (item in items) {
        views.add(toUtilizationOverrideView(item))
    }
    return views
}

fun sumUtilizationOverrideView(view: UtilizationOverrideView): Double {
    val parsedStopCount = view.stopCount.toInt()
    val parsedWindowDays = view.windowDaysText.toInt()
    return parsedStopCount.toDouble() + parsedWindowDays.toDouble()
}
