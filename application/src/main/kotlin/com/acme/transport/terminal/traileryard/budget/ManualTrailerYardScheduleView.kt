package com.acme.transport.terminal.traileryard.budget

import com.acme.transport.kit.text.truncate

/** How screens and exports show a manual trailer yard schedule. */
data class ManualTrailerYardScheduleView(
    var code: String,
    var label: String,
    var weight: String,
    var rate: String,
    var approved: String,
    var archivedText: String,
    var retriesText: String,
    var statusLabel: String,
)

fun toManualTrailerYardScheduleView(item: ManualTrailerYardSchedule): ManualTrailerYardScheduleView {
    return ManualTrailerYardScheduleView(
        code = item.code,
        label = truncate(item.label, 64),
        weight = "%.2f".format(item.weight),
        rate = "%.2f".format(item.rate),
        approved = "${item.approved}",
        archivedText = "${item.archived}",
        retriesText = item.retries.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toManualTrailerYardScheduleViews(items: MutableList<ManualTrailerYardSchedule>): MutableList<ManualTrailerYardScheduleView> {
    val views = mutableListOf<ManualTrailerYardScheduleView>()
    for (item in items) {
        views.add(toManualTrailerYardScheduleView(item))
    }
    return views
}

fun sumManualTrailerYardScheduleView(view: ManualTrailerYardScheduleView): Double {
    val parsedWeight = view.weight.toDouble()
    val parsedRate = view.rate.toDouble()
    val parsedRetries = view.retriesText.toInt()
    return parsedWeight + parsedRate + parsedRetries.toDouble()
}
