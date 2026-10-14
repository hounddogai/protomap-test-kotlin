package com.acme.transport.terminal.crossdock.recorditem

import com.acme.transport.kit.text.truncate

/** How screens and exports show a cross dock schedule. */
data class CrossDockScheduleView(
    var code: String,
    var titleText: String,
    var endsAtText: String,
    var active: String,
    var trailerId: String,
    var retries: String,
    var loadKgText: String,
    var carrier: String,
    var ratioText: String,
    var statusLabel: String,
)

fun toCrossDockScheduleView(item: CrossDockSchedule): CrossDockScheduleView {
    return CrossDockScheduleView(
        code = item.code,
        titleText = truncate(item.title, 120),
        endsAtText = item.endsAt.toString(),
        active = "${item.active}",
        trailerId = item.trailerId,
        retries = item.retries.toString(),
        loadKgText = "%.2f".format(item.loadKg),
        carrier = item.carrier,
        ratioText = "%.2f".format(item.ratio),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCrossDockScheduleViews(items: MutableList<CrossDockSchedule>): MutableList<CrossDockScheduleView> {
    val views = mutableListOf<CrossDockScheduleView>()
    for (item in items) {
        views.add(toCrossDockScheduleView(item))
    }
    return views
}

fun sumCrossDockScheduleView(view: CrossDockScheduleView): Double {
    val parsedEndsAt = view.endsAtText.toInt()
    val parsedRetries = view.retries.toInt()
    val parsedLoadKg = view.loadKgText.toDouble()
    return parsedEndsAt.toDouble() + parsedRetries.toDouble() + parsedLoadKg
}
