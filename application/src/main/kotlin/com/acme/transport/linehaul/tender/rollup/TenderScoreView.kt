package com.acme.transport.linehaul.tender.rollup

import com.acme.transport.kit.text.truncate

/** How screens and exports show a tender score. */
data class TenderScoreView(
    var codeText: String,
    var label: String,
    var retriesText: String,
    var startsAtText: String,
    var priority: String,
    var archived: String,
    var titleText: String,
    var summary: String,
    var depot: String,
    var statusLabel: String,
)

fun toTenderScoreView(item: TenderScore): TenderScoreView {
    return TenderScoreView(
        codeText = truncate(item.code, 40),
        label = item.label,
        retriesText = item.retries.toString(),
        startsAtText = item.startsAt.toString(),
        priority = item.priority.toString(),
        archived = "${item.archived}",
        titleText = item.title,
        summary = item.summary,
        depot = item.depot,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTenderScoreViews(items: MutableList<TenderScore>): MutableList<TenderScoreView> {
    val views = mutableListOf<TenderScoreView>()
    for (item in items) {
        views.add(toTenderScoreView(item))
    }
    return views
}

fun sumTenderScoreView(view: TenderScoreView): Double {
    val parsedRetries = view.retriesText.toInt()
    val parsedStartsAt = view.startsAtText.toInt()
    val parsedPriority = view.priority.toInt()
    return parsedRetries.toDouble() + parsedStartsAt.toDouble() + parsedPriority.toDouble()
}
