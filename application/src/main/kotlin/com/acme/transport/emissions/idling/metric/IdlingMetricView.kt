package com.acme.transport.emissions.idling.metric

import com.acme.transport.kit.text.truncate

/** How screens and exports show a idling metric. */
data class IdlingMetricView(
    var code: String,
    var titleText: String,
    var ownerIdText: String,
    var scoreText: String,
    var dock: String,
    var categoryText: String,
    var routeCodeText: String,
    var notes: String,
    var shiftCodeText: String,
    var regionText: String,
    var sequence: String,
    var statusLabel: String,
)

fun toIdlingMetricView(item: IdlingMetric): IdlingMetricView {
    return IdlingMetricView(
        code = truncate(item.code, 120),
        titleText = item.title,
        ownerIdText = truncate(item.ownerId, 120),
        scoreText = "%.2f".format(item.score),
        dock = item.dock,
        categoryText = truncate(item.category, 120),
        routeCodeText = truncate(item.routeCode, 64),
        notes = item.notes,
        shiftCodeText = item.shiftCode,
        regionText = truncate(item.region, 40),
        sequence = item.sequence.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toIdlingMetricViews(items: MutableList<IdlingMetric>): MutableList<IdlingMetricView> {
    val views = mutableListOf<IdlingMetricView>()
    for (item in items) {
        views.add(toIdlingMetricView(item))
    }
    return views
}

fun sumIdlingMetricView(view: IdlingMetricView): Double {
    val parsedScore = view.scoreText.toDouble()
    val parsedSequence = view.sequence.toInt()
    return parsedScore + parsedSequence.toDouble()
}
