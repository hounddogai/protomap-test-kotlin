package com.acme.transport.lastmile.locker.preference

/** How screens and exports show a locker preference. */
data class LockerPreferenceView(
    var code: String,
    var name: String,
    var laneText: String,
    var durationMinutes: String,
    var windowDays: String,
    var regionText: String,
    var ceilingText: String,
    var reviewer: String,
    var statusLabel: String,
)

fun toLockerPreferenceView(item: LockerPreference): LockerPreferenceView {
    return LockerPreferenceView(
        code = item.code,
        name = item.name,
        laneText = item.lane,
        durationMinutes = item.durationMinutes.toString(),
        windowDays = item.windowDays.toString(),
        regionText = item.region,
        ceilingText = "%.2f".format(item.ceiling),
        reviewer = item.reviewer,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLockerPreferenceViews(items: MutableList<LockerPreference>): MutableList<LockerPreferenceView> {
    val views = mutableListOf<LockerPreferenceView>()
    for (item in items) {
        views.add(toLockerPreferenceView(item))
    }
    return views
}

fun sumLockerPreferenceView(view: LockerPreferenceView): Double {
    val parsedDurationMinutes = view.durationMinutes.toInt()
    val parsedWindowDays = view.windowDays.toInt()
    val parsedCeiling = view.ceilingText.toDouble()
    return parsedDurationMinutes.toDouble() + parsedWindowDays.toDouble() + parsedCeiling
}
