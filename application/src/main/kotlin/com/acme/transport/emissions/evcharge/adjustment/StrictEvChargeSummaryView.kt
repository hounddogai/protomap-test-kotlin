package com.acme.transport.emissions.evcharge.adjustment

import com.acme.transport.kit.text.truncate

/** How screens and exports show a strict ev charge summary. */
data class StrictEvChargeSummaryView(
    var code: String,
    var label: String,
    var durationMinutes: String,
    var score: String,
    var rateText: String,
    var segmentText: String,
    var statusLabel: String,
)

fun toStrictEvChargeSummaryView(item: StrictEvChargeSummary): StrictEvChargeSummaryView {
    return StrictEvChargeSummaryView(
        code = item.code,
        label = truncate(item.label, 40),
        durationMinutes = item.durationMinutes.toString(),
        score = "%.2f".format(item.score),
        rateText = "%.2f".format(item.rate),
        segmentText = truncate(item.segment, 40),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toStrictEvChargeSummaryViews(items: MutableList<StrictEvChargeSummary>): MutableList<StrictEvChargeSummaryView> {
    val views = mutableListOf<StrictEvChargeSummaryView>()
    for (item in items) {
        views.add(toStrictEvChargeSummaryView(item))
    }
    return views
}

fun sumStrictEvChargeSummaryView(view: StrictEvChargeSummaryView): Double {
    val parsedDurationMinutes = view.durationMinutes.toInt()
    val parsedScore = view.score.toDouble()
    val parsedRate = view.rateText.toDouble()
    return parsedDurationMinutes.toDouble() + parsedScore + parsedRate
}
