package com.acme.transport.crewing.restbreak.regionalbreakpreference

import com.acme.transport.kit.text.truncate

/** How screens and exports show a default rest break schedule. */
data class DefaultRestBreakScheduleView(
    var codeText: String,
    var label: String,
    var distanceKmText: String,
    var channel: String,
    var durationMinutesText: String,
    var pinned: String,
    var statusLabel: String,
)

fun toDefaultRestBreakScheduleView(item: DefaultRestBreakSchedule): DefaultRestBreakScheduleView {
    return DefaultRestBreakScheduleView(
        codeText = item.code,
        label = item.label,
        distanceKmText = "%.2f".format(item.distanceKm),
        channel = truncate(item.channel, 80),
        durationMinutesText = item.durationMinutes.toString(),
        pinned = "${item.pinned}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDefaultRestBreakScheduleViews(items: MutableList<DefaultRestBreakSchedule>): MutableList<DefaultRestBreakScheduleView> {
    val views = mutableListOf<DefaultRestBreakScheduleView>()
    for (item in items) {
        views.add(toDefaultRestBreakScheduleView(item))
    }
    return views
}

fun sumDefaultRestBreakScheduleView(view: DefaultRestBreakScheduleView): Double {
    val parsedDistanceKm = view.distanceKmText.toDouble()
    val parsedDurationMinutes = view.durationMinutesText.toInt()
    return parsedDistanceKm + parsedDurationMinutes.toDouble()
}
