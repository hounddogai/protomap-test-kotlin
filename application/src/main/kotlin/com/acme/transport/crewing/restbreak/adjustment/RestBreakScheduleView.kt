package com.acme.transport.crewing.restbreak.adjustment

import com.acme.transport.kit.text.truncate

/** How screens and exports show a rest break schedule. */
data class RestBreakScheduleView(
    var codeText: String,
    var label: String,
    var descriptionText: String,
    var channel: String,
    var cost: String,
    var carrier: String,
    var summary: String,
    var archived: String,
    var weight: String,
    var dock: String,
    var statusLabel: String,
)

fun toRestBreakScheduleView(item: RestBreakSchedule): RestBreakScheduleView {
    return RestBreakScheduleView(
        codeText = truncate(item.code, 80),
        label = truncate(item.label, 80),
        descriptionText = item.description,
        channel = item.channel,
        cost = "%.2f".format(item.cost),
        carrier = truncate(item.carrier, 120),
        summary = item.summary,
        archived = "${item.archived}",
        weight = "%.2f".format(item.weight),
        dock = item.dock,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRestBreakScheduleViews(items: MutableList<RestBreakSchedule>): MutableList<RestBreakScheduleView> {
    val views = mutableListOf<RestBreakScheduleView>()
    for (item in items) {
        views.add(toRestBreakScheduleView(item))
    }
    return views
}

fun sumRestBreakScheduleView(view: RestBreakScheduleView): Double {
    val parsedCost = view.cost.toDouble()
    val parsedWeight = view.weight.toDouble()
    return parsedCost + parsedWeight
}
