package com.acme.transport.safety.scorecard.scorecardslot

import com.acme.transport.kit.text.truncate

/** How screens and exports show a scorecard preference. */
data class ScorecardPreferenceView(
    var code: String,
    var title: String,
    var capacityText: String,
    var weightText: String,
    var revision: String,
    var batchSizeText: String,
    var loadKgText: String,
    var summaryText: String,
    var updatedAt: String,
    var locale: String,
    var reviewer: String,
    var statusLabel: String,
)

fun toScorecardPreferenceView(item: ScorecardPreference): ScorecardPreferenceView {
    return ScorecardPreferenceView(
        code = item.code,
        title = item.title,
        capacityText = item.capacity.toString(),
        weightText = "%.2f".format(item.weight),
        revision = item.revision.toString(),
        batchSizeText = item.batchSize.toString(),
        loadKgText = "%.2f".format(item.loadKg),
        summaryText = truncate(item.summary, 64),
        updatedAt = item.updatedAt.toString(),
        locale = truncate(item.locale, 40),
        reviewer = item.reviewer,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toScorecardPreferenceViews(items: MutableList<ScorecardPreference>): MutableList<ScorecardPreferenceView> {
    val views = mutableListOf<ScorecardPreferenceView>()
    for (item in items) {
        views.add(toScorecardPreferenceView(item))
    }
    return views
}

fun sumScorecardPreferenceView(view: ScorecardPreferenceView): Double {
    val parsedCapacity = view.capacityText.toInt()
    val parsedWeight = view.weightText.toDouble()
    val parsedRevision = view.revision.toInt()
    return parsedCapacity.toDouble() + parsedWeight + parsedRevision.toDouble()
}
