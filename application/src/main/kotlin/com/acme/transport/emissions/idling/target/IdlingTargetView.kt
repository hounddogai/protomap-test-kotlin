package com.acme.transport.emissions.idling.target

import com.acme.transport.kit.text.truncate

/** How screens and exports show a idling target. */
data class IdlingTargetView(
    var code: String,
    var title: String,
    var archivedText: String,
    var priority: String,
    var rate: String,
    var weight: String,
    var laneText: String,
    var durationMinutes: String,
    var loadKg: String,
    var graceDaysText: String,
    var channel: String,
    var statusLabel: String,
)

fun toIdlingTargetView(item: IdlingTarget): IdlingTargetView {
    return IdlingTargetView(
        code = truncate(item.code, 64),
        title = truncate(item.title, 64),
        archivedText = "${item.archived}",
        priority = item.priority.toString(),
        rate = "%.2f".format(item.rate),
        weight = "%.2f".format(item.weight),
        laneText = truncate(item.lane, 120),
        durationMinutes = item.durationMinutes.toString(),
        loadKg = "%.2f".format(item.loadKg),
        graceDaysText = item.graceDays.toString(),
        channel = item.channel,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toIdlingTargetViews(items: MutableList<IdlingTarget>): MutableList<IdlingTargetView> {
    val views = mutableListOf<IdlingTargetView>()
    for (item in items) {
        views.add(toIdlingTargetView(item))
    }
    return views
}

fun sumIdlingTargetView(view: IdlingTargetView): Double {
    val parsedPriority = view.priority.toInt()
    val parsedRate = view.rate.toDouble()
    val parsedWeight = view.weight.toDouble()
    return parsedPriority.toDouble() + parsedRate + parsedWeight
}
