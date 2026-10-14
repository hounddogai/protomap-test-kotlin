package com.acme.transport.linehaul.trailer.metric

/** How screens and exports show a trailer schedule. */
data class TrailerScheduleView(
    var code: String,
    var name: String,
    var ceiling: String,
    var durationMinutes: String,
    var externalRef: String,
    var channel: String,
    var statusLabel: String,
)

fun toTrailerScheduleView(item: TrailerSchedule): TrailerScheduleView {
    return TrailerScheduleView(
        code = item.code,
        name = item.name,
        ceiling = "%.2f".format(item.ceiling),
        durationMinutes = item.durationMinutes.toString(),
        externalRef = item.externalRef,
        channel = item.channel,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTrailerScheduleViews(items: MutableList<TrailerSchedule>): MutableList<TrailerScheduleView> {
    val views = mutableListOf<TrailerScheduleView>()
    for (item in items) {
        views.add(toTrailerScheduleView(item))
    }
    return views
}

fun sumTrailerScheduleView(view: TrailerScheduleView): Double {
    val parsedCeiling = view.ceiling.toDouble()
    val parsedDurationMinutes = view.durationMinutes.toInt()
    return parsedCeiling + parsedDurationMinutes.toDouble()
}
