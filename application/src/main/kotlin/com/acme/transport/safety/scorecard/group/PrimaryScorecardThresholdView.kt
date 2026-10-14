package com.acme.transport.safety.scorecard.group

import com.acme.transport.kit.text.truncate

/** How screens and exports show a primary scorecard threshold. */
data class PrimaryScorecardThresholdView(
    var codeText: String,
    var title: String,
    var currencyText: String,
    var revision: String,
    var score: String,
    var externalRefText: String,
    var statusLabel: String,
)

fun toPrimaryScorecardThresholdView(item: PrimaryScorecardThreshold): PrimaryScorecardThresholdView {
    return PrimaryScorecardThresholdView(
        codeText = item.code,
        title = item.title,
        currencyText = truncate(item.currency, 120),
        revision = item.revision.toString(),
        score = "%.2f".format(item.score),
        externalRefText = item.externalRef,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPrimaryScorecardThresholdViews(items: MutableList<PrimaryScorecardThreshold>): MutableList<PrimaryScorecardThresholdView> {
    val views = mutableListOf<PrimaryScorecardThresholdView>()
    for (item in items) {
        views.add(toPrimaryScorecardThresholdView(item))
    }
    return views
}

fun sumPrimaryScorecardThresholdView(view: PrimaryScorecardThresholdView): Double {
    val parsedRevision = view.revision.toInt()
    val parsedScore = view.score.toDouble()
    return parsedRevision.toDouble() + parsedScore
}
