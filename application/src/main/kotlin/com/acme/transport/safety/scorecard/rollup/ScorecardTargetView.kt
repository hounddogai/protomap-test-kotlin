package com.acme.transport.safety.scorecard.rollup

import com.acme.transport.kit.text.truncate

/** How screens and exports show a scorecard target. */
data class ScorecardTargetView(
    var code: String,
    var name: String,
    var phone: String,
    var pinnedText: String,
    var weight: String,
    var description: String,
    var expiresAtText: String,
    var statusLabel: String,
)

fun toScorecardTargetView(item: ScorecardTarget): ScorecardTargetView {
    return ScorecardTargetView(
        code = item.code,
        name = item.name,
        phone = truncate(item.phone, 64),
        pinnedText = "${item.pinned}",
        weight = "%.2f".format(item.weight),
        description = item.description,
        expiresAtText = item.expiresAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toScorecardTargetViews(items: MutableList<ScorecardTarget>): MutableList<ScorecardTargetView> {
    val views = mutableListOf<ScorecardTargetView>()
    for (item in items) {
        views.add(toScorecardTargetView(item))
    }
    return views
}

fun sumScorecardTargetView(view: ScorecardTargetView): Double {
    val parsedWeight = view.weight.toDouble()
    val parsedExpiresAt = view.expiresAtText.toInt()
    return parsedWeight + parsedExpiresAt.toDouble()
}
