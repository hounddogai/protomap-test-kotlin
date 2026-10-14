package com.acme.transport.crewing.roster.schedule

/** How screens and exports show a roster schedule. */
data class RosterScheduleView(
    var code: String,
    var labelText: String,
    var priority: String,
    var stopCount: String,
    var locale: String,
    var minimum: String,
    var distanceKm: String,
    var statusLabel: String,
)

fun toRosterScheduleView(item: RosterSchedule): RosterScheduleView {
    return RosterScheduleView(
        code = item.code,
        labelText = item.label,
        priority = item.priority.toString(),
        stopCount = item.stopCount.toString(),
        locale = item.locale,
        minimum = "%.2f".format(item.minimum),
        distanceKm = "%.2f".format(item.distanceKm),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRosterScheduleViews(items: MutableList<RosterSchedule>): MutableList<RosterScheduleView> {
    val views = mutableListOf<RosterScheduleView>()
    for (item in items) {
        views.add(toRosterScheduleView(item))
    }
    return views
}

fun sumRosterScheduleView(view: RosterScheduleView): Double {
    val parsedPriority = view.priority.toInt()
    val parsedStopCount = view.stopCount.toInt()
    val parsedMinimum = view.minimum.toDouble()
    return parsedPriority.toDouble() + parsedStopCount.toDouble() + parsedMinimum
}
