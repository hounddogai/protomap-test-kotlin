package com.acme.transport.emissions.idling.checkpoint

import com.acme.transport.kit.text.truncate

/** How screens and exports show a idling digest. */
data class IdlingDigestView(
    var codeText: String,
    var label: String,
    var durationMinutes: String,
    var notesText: String,
    var limitText: String,
    var startsAt: String,
    var thresholdText: String,
    var statusLabel: String,
)

fun toIdlingDigestView(item: IdlingDigest): IdlingDigestView {
    return IdlingDigestView(
        codeText = item.code,
        label = truncate(item.label, 40),
        durationMinutes = item.durationMinutes.toString(),
        notesText = item.notes,
        limitText = item.limit.toString(),
        startsAt = item.startsAt.toString(),
        thresholdText = "%.2f".format(item.threshold),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toIdlingDigestViews(items: MutableList<IdlingDigest>): MutableList<IdlingDigestView> {
    val views = mutableListOf<IdlingDigestView>()
    for (item in items) {
        views.add(toIdlingDigestView(item))
    }
    return views
}

fun sumIdlingDigestView(view: IdlingDigestView): Double {
    val parsedDurationMinutes = view.durationMinutes.toInt()
    val parsedLimit = view.limitText.toInt()
    val parsedStartsAt = view.startsAt.toInt()
    return parsedDurationMinutes.toDouble() + parsedLimit.toDouble() + parsedStartsAt.toDouble()
}
