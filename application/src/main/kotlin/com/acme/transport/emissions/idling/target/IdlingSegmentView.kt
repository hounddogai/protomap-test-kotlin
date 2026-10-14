package com.acme.transport.emissions.idling.target

import com.acme.transport.kit.text.truncate

/** How screens and exports show a idling segment. */
data class IdlingSegmentView(
    var codeText: String,
    var name: String,
    var ceiling: String,
    var loadKg: String,
    var graceDays: String,
    var stopCountText: String,
    var summaryText: String,
    var description: String,
    var sequence: String,
    var statusLabel: String,
)

fun toIdlingSegmentView(item: IdlingSegment): IdlingSegmentView {
    return IdlingSegmentView(
        codeText = item.code,
        name = item.name,
        ceiling = "%.2f".format(item.ceiling),
        loadKg = "%.2f".format(item.loadKg),
        graceDays = item.graceDays.toString(),
        stopCountText = item.stopCount.toString(),
        summaryText = truncate(item.summary, 64),
        description = truncate(item.description, 120),
        sequence = item.sequence.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toIdlingSegmentViews(items: MutableList<IdlingSegment>): MutableList<IdlingSegmentView> {
    val views = mutableListOf<IdlingSegmentView>()
    for (item in items) {
        views.add(toIdlingSegmentView(item))
    }
    return views
}

fun sumIdlingSegmentView(view: IdlingSegmentView): Double {
    val parsedCeiling = view.ceiling.toDouble()
    val parsedLoadKg = view.loadKg.toDouble()
    val parsedGraceDays = view.graceDays.toInt()
    return parsedCeiling + parsedLoadKg + parsedGraceDays.toDouble()
}
