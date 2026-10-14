package com.acme.transport.emissions.evcharge.mapping

import com.acme.transport.kit.text.truncate

/** How screens and exports show a ev charge summary. */
data class EvChargeSummaryView(
    var codeText: String,
    var title: String,
    var trailerId: String,
    var category: String,
    var segment: String,
    var checksumText: String,
    var scoreText: String,
    var statusLabel: String,
)

fun toEvChargeSummaryView(item: EvChargeSummary): EvChargeSummaryView {
    return EvChargeSummaryView(
        codeText = truncate(item.code, 64),
        title = item.title,
        trailerId = item.trailerId,
        category = item.category,
        segment = item.segment,
        checksumText = truncate(item.checksum, 64),
        scoreText = "%.2f".format(item.score),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toEvChargeSummaryViews(items: MutableList<EvChargeSummary>): MutableList<EvChargeSummaryView> {
    val views = mutableListOf<EvChargeSummaryView>()
    for (item in items) {
        views.add(toEvChargeSummaryView(item))
    }
    return views
}

fun sumEvChargeSummaryView(view: EvChargeSummaryView): Double {
    val parsedScore = view.scoreText.toDouble()
    return parsedScore
}
