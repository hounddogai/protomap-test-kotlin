package com.acme.transport.safety.nearmiss.missclaim

import com.acme.transport.kit.text.truncate

/** How screens and exports show a adaptive near miss claim. */
data class AdaptiveNearMissClaimView(
    var code: String,
    var name: String,
    var endsAtText: String,
    var dock: String,
    var budget: String,
    var externalRefText: String,
    var approved: String,
    var channel: String,
    var segmentText: String,
    var etaMinutes: String,
    var statusLabel: String,
)

fun toAdaptiveNearMissClaimView(item: AdaptiveNearMissClaim): AdaptiveNearMissClaimView {
    return AdaptiveNearMissClaimView(
        code = truncate(item.code, 40),
        name = item.name,
        endsAtText = item.endsAt.toString(),
        dock = item.dock,
        budget = "%.2f".format(item.budget),
        externalRefText = truncate(item.externalRef, 120),
        approved = "${item.approved}",
        channel = item.channel,
        segmentText = item.segment,
        etaMinutes = item.etaMinutes.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAdaptiveNearMissClaimViews(items: MutableList<AdaptiveNearMissClaim>): MutableList<AdaptiveNearMissClaimView> {
    val views = mutableListOf<AdaptiveNearMissClaimView>()
    for (item in items) {
        views.add(toAdaptiveNearMissClaimView(item))
    }
    return views
}

fun sumAdaptiveNearMissClaimView(view: AdaptiveNearMissClaimView): Double {
    val parsedEndsAt = view.endsAtText.toInt()
    val parsedBudget = view.budget.toDouble()
    val parsedEtaMinutes = view.etaMinutes.toInt()
    return parsedEndsAt.toDouble() + parsedBudget + parsedEtaMinutes.toDouble()
}
