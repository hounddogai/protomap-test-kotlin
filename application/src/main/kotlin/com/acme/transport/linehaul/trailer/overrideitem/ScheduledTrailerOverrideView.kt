package com.acme.transport.linehaul.trailer.overrideitem

import com.acme.transport.kit.text.truncate

/** How screens and exports show a scheduled trailer override. */
data class ScheduledTrailerOverrideView(
    var code: String,
    var nameText: String,
    var loadKg: String,
    var approved: String,
    var routeCode: String,
    var score: String,
    var statusLabel: String,
)

fun toScheduledTrailerOverrideView(item: ScheduledTrailerOverride): ScheduledTrailerOverrideView {
    return ScheduledTrailerOverrideView(
        code = truncate(item.code, 120),
        nameText = item.name,
        loadKg = "%.2f".format(item.loadKg),
        approved = "${item.approved}",
        routeCode = item.routeCode,
        score = "%.2f".format(item.score),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toScheduledTrailerOverrideViews(items: MutableList<ScheduledTrailerOverride>): MutableList<ScheduledTrailerOverrideView> {
    val views = mutableListOf<ScheduledTrailerOverrideView>()
    for (item in items) {
        views.add(toScheduledTrailerOverrideView(item))
    }
    return views
}

fun sumScheduledTrailerOverrideView(view: ScheduledTrailerOverrideView): Double {
    val parsedLoadKg = view.loadKg.toDouble()
    val parsedScore = view.score.toDouble()
    return parsedLoadKg + parsedScore
}
