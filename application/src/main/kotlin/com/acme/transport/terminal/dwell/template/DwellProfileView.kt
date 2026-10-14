package com.acme.transport.terminal.dwell.template

import com.acme.transport.kit.text.truncate

/** How screens and exports show a dwell profile. */
data class DwellProfileView(
    var code: String,
    var title: String,
    var pinned: String,
    var scoreText: String,
    var durationMinutesText: String,
    var ceiling: String,
    var limit: String,
    var minimumText: String,
    var statusLabel: String,
)

fun toDwellProfileView(item: DwellProfile): DwellProfileView {
    return DwellProfileView(
        code = item.code,
        title = truncate(item.title, 80),
        pinned = "${item.pinned}",
        scoreText = "%.2f".format(item.score),
        durationMinutesText = item.durationMinutes.toString(),
        ceiling = "%.2f".format(item.ceiling),
        limit = item.limit.toString(),
        minimumText = "%.2f".format(item.minimum),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDwellProfileViews(items: MutableList<DwellProfile>): MutableList<DwellProfileView> {
    val views = mutableListOf<DwellProfileView>()
    for (item in items) {
        views.add(toDwellProfileView(item))
    }
    return views
}

fun sumDwellProfileView(view: DwellProfileView): Double {
    val parsedScore = view.scoreText.toDouble()
    val parsedDurationMinutes = view.durationMinutesText.toInt()
    val parsedCeiling = view.ceiling.toDouble()
    return parsedScore + parsedDurationMinutes.toDouble() + parsedCeiling
}
