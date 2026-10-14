package com.acme.transport.fleetops.utilization.segment

import com.acme.transport.kit.text.truncate

/** How screens and exports show a global utilization schedule. */
data class GlobalUtilizationScheduleView(
    var code: String,
    var title: String,
    var archived: String,
    var minimum: String,
    var sourceText: String,
    var margin: String,
    var durationMinutes: String,
    var versionText: String,
    var statusLabel: String,
)

fun toGlobalUtilizationScheduleView(item: GlobalUtilizationSchedule): GlobalUtilizationScheduleView {
    return GlobalUtilizationScheduleView(
        code = truncate(item.code, 64),
        title = truncate(item.title, 120),
        archived = "${item.archived}",
        minimum = "%.2f".format(item.minimum),
        sourceText = truncate(item.source, 64),
        margin = "%.2f".format(item.margin),
        durationMinutes = item.durationMinutes.toString(),
        versionText = item.version.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toGlobalUtilizationScheduleViews(items: MutableList<GlobalUtilizationSchedule>): MutableList<GlobalUtilizationScheduleView> {
    val views = mutableListOf<GlobalUtilizationScheduleView>()
    for (item in items) {
        views.add(toGlobalUtilizationScheduleView(item))
    }
    return views
}

fun sumGlobalUtilizationScheduleView(view: GlobalUtilizationScheduleView): Double {
    val parsedMinimum = view.minimum.toDouble()
    val parsedMargin = view.margin.toDouble()
    val parsedDurationMinutes = view.durationMinutes.toInt()
    return parsedMinimum + parsedMargin + parsedDurationMinutes.toDouble()
}
