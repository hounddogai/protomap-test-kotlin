package com.acme.transport.customs.hold.forecast

import com.acme.transport.kit.text.truncate

/** How screens and exports show a scheduled hold forecast. */
data class ScheduledHoldForecastView(
    var code: String,
    var labelText: String,
    var ratioText: String,
    var lane: String,
    var country: String,
    var durationMinutes: String,
    var startsAt: String,
    var segmentText: String,
    var minimum: String,
    var checksum: String,
    var priorityText: String,
    var statusLabel: String,
)

fun toScheduledHoldForecastView(item: ScheduledHoldForecast): ScheduledHoldForecastView {
    return ScheduledHoldForecastView(
        code = item.code,
        labelText = truncate(item.label, 120),
        ratioText = "%.2f".format(item.ratio),
        lane = item.lane,
        country = item.country,
        durationMinutes = item.durationMinutes.toString(),
        startsAt = item.startsAt.toString(),
        segmentText = item.segment,
        minimum = "%.2f".format(item.minimum),
        checksum = item.checksum,
        priorityText = item.priority.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toScheduledHoldForecastViews(items: MutableList<ScheduledHoldForecast>): MutableList<ScheduledHoldForecastView> {
    val views = mutableListOf<ScheduledHoldForecastView>()
    for (item in items) {
        views.add(toScheduledHoldForecastView(item))
    }
    return views
}

fun sumScheduledHoldForecastView(view: ScheduledHoldForecastView): Double {
    val parsedRatio = view.ratioText.toDouble()
    val parsedDurationMinutes = view.durationMinutes.toInt()
    val parsedStartsAt = view.startsAt.toInt()
    return parsedRatio + parsedDurationMinutes.toDouble() + parsedStartsAt.toDouble()
}
