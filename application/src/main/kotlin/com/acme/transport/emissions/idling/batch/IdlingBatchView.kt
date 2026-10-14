package com.acme.transport.emissions.idling.batch

import com.acme.transport.kit.text.truncate

/** How screens and exports show a idling batch. */
data class IdlingBatchView(
    var code: String,
    var title: String,
    var pinnedText: String,
    var odometer: String,
    var attemptsText: String,
    var statusLabel: String,
)

fun toIdlingBatchView(item: IdlingBatch): IdlingBatchView {
    return IdlingBatchView(
        code = truncate(item.code, 120),
        title = truncate(item.title, 120),
        pinnedText = "${item.pinned}",
        odometer = item.odometer.toString(),
        attemptsText = item.attempts.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toIdlingBatchViews(items: MutableList<IdlingBatch>): MutableList<IdlingBatchView> {
    val views = mutableListOf<IdlingBatchView>()
    for (item in items) {
        views.add(toIdlingBatchView(item))
    }
    return views
}

fun sumIdlingBatchView(view: IdlingBatchView): Double {
    val parsedOdometer = view.odometer.toInt()
    val parsedAttempts = view.attemptsText.toInt()
    return parsedOdometer.toDouble() + parsedAttempts.toDouble()
}
