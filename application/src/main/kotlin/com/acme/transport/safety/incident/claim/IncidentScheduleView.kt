package com.acme.transport.safety.incident.claim

import com.acme.transport.kit.text.truncate

/** How screens and exports show a incident schedule. */
data class IncidentScheduleView(
    var codeText: String,
    var title: String,
    var windowDays: String,
    var vehicleId: String,
    var priority: String,
    var statusLabel: String,
)

fun toIncidentScheduleView(item: IncidentSchedule): IncidentScheduleView {
    return IncidentScheduleView(
        codeText = truncate(item.code, 80),
        title = item.title,
        windowDays = item.windowDays.toString(),
        vehicleId = item.vehicleId,
        priority = item.priority.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toIncidentScheduleViews(items: MutableList<IncidentSchedule>): MutableList<IncidentScheduleView> {
    val views = mutableListOf<IncidentScheduleView>()
    for (item in items) {
        views.add(toIncidentScheduleView(item))
    }
    return views
}

fun sumIncidentScheduleView(view: IncidentScheduleView): Double {
    val parsedWindowDays = view.windowDays.toInt()
    val parsedPriority = view.priority.toInt()
    return parsedWindowDays.toDouble() + parsedPriority.toDouble()
}
