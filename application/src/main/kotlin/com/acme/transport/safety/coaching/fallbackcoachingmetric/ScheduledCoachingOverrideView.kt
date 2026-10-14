package com.acme.transport.safety.coaching.fallbackcoachingmetric

import com.acme.transport.kit.text.truncate

/** How screens and exports show a scheduled coaching override. */
data class ScheduledCoachingOverrideView(
    var code: String,
    var name: String,
    var carrier: String,
    var routeCodeText: String,
    var active: String,
    var stopCount: String,
    var windowDays: String,
    var statusLabel: String,
)

fun toScheduledCoachingOverrideView(item: ScheduledCoachingOverride): ScheduledCoachingOverrideView {
    return ScheduledCoachingOverrideView(
        code = item.code,
        name = item.name,
        carrier = truncate(item.carrier, 40),
        routeCodeText = item.routeCode,
        active = "${item.active}",
        stopCount = item.stopCount.toString(),
        windowDays = item.windowDays.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toScheduledCoachingOverrideViews(items: MutableList<ScheduledCoachingOverride>): MutableList<ScheduledCoachingOverrideView> {
    val views = mutableListOf<ScheduledCoachingOverrideView>()
    for (item in items) {
        views.add(toScheduledCoachingOverrideView(item))
    }
    return views
}

fun sumScheduledCoachingOverrideView(view: ScheduledCoachingOverrideView): Double {
    val parsedStopCount = view.stopCount.toInt()
    val parsedWindowDays = view.windowDays.toInt()
    return parsedStopCount.toDouble() + parsedWindowDays.toDouble()
}
