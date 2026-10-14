package com.acme.transport.safety.scorecard.entry

import com.acme.transport.kit.text.truncate

/** How screens and exports show a tiered scorecard entry. */
data class TieredScorecardEntryView(
    var code: String,
    var name: String,
    var active: String,
    var revisionText: String,
    var externalRef: String,
    var retriesText: String,
    var rateText: String,
    var statusLabel: String,
)

fun toTieredScorecardEntryView(item: TieredScorecardEntry): TieredScorecardEntryView {
    return TieredScorecardEntryView(
        code = item.code,
        name = item.name,
        active = "${item.active}",
        revisionText = item.revision.toString(),
        externalRef = truncate(item.externalRef, 120),
        retriesText = item.retries.toString(),
        rateText = "%.2f".format(item.rate),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTieredScorecardEntryViews(items: MutableList<TieredScorecardEntry>): MutableList<TieredScorecardEntryView> {
    val views = mutableListOf<TieredScorecardEntryView>()
    for (item in items) {
        views.add(toTieredScorecardEntryView(item))
    }
    return views
}

fun sumTieredScorecardEntryView(view: TieredScorecardEntryView): Double {
    val parsedRevision = view.revisionText.toInt()
    val parsedRetries = view.retriesText.toInt()
    val parsedRate = view.rateText.toDouble()
    return parsedRevision.toDouble() + parsedRetries.toDouble() + parsedRate
}
