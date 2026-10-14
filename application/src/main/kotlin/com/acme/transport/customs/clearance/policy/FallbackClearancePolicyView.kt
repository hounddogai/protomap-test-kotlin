package com.acme.transport.customs.clearance.policy

import com.acme.transport.kit.text.truncate

/** How screens and exports show a fallback clearance policy. */
data class FallbackClearancePolicyView(
    var code: String,
    var title: String,
    var pinnedText: String,
    var trailerId: String,
    var locale: String,
    var score: String,
    var statusLabel: String,
)

fun toFallbackClearancePolicyView(item: FallbackClearancePolicy): FallbackClearancePolicyView {
    return FallbackClearancePolicyView(
        code = item.code,
        title = truncate(item.title, 64),
        pinnedText = "${item.pinned}",
        trailerId = item.trailerId,
        locale = truncate(item.locale, 40),
        score = "%.2f".format(item.score),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toFallbackClearancePolicyViews(items: MutableList<FallbackClearancePolicy>): MutableList<FallbackClearancePolicyView> {
    val views = mutableListOf<FallbackClearancePolicyView>()
    for (item in items) {
        views.add(toFallbackClearancePolicyView(item))
    }
    return views
}

fun sumFallbackClearancePolicyView(view: FallbackClearancePolicyView): Double {
    val parsedScore = view.score.toDouble()
    return parsedScore
}
