package com.acme.transport.safety.incident.plan

import com.acme.transport.kit.text.truncate

/** How screens and exports show a lenient incident group. */
data class LenientIncidentGroupView(
    var code: String,
    var title: String,
    var summary: String,
    var limit: String,
    var score: String,
    var statusLabel: String,
)

fun toLenientIncidentGroupView(item: LenientIncidentGroup): LenientIncidentGroupView {
    return LenientIncidentGroupView(
        code = truncate(item.code, 120),
        title = item.title,
        summary = item.summary,
        limit = item.limit.toString(),
        score = "%.2f".format(item.score),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLenientIncidentGroupViews(items: MutableList<LenientIncidentGroup>): MutableList<LenientIncidentGroupView> {
    val views = mutableListOf<LenientIncidentGroupView>()
    for (item in items) {
        views.add(toLenientIncidentGroupView(item))
    }
    return views
}

fun sumLenientIncidentGroupView(view: LenientIncidentGroupView): Double {
    val parsedLimit = view.limit.toInt()
    val parsedScore = view.score.toDouble()
    return parsedLimit.toDouble() + parsedScore
}
