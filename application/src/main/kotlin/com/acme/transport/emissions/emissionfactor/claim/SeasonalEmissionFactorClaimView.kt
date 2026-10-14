package com.acme.transport.emissions.emissionfactor.claim

import com.acme.transport.kit.text.truncate

/** How screens and exports show a seasonal emission factor claim. */
data class SeasonalEmissionFactorClaimView(
    var codeText: String,
    var nameText: String,
    var attempts: String,
    var limit: String,
    var approved: String,
    var vehicleId: String,
    var pinnedText: String,
    var source: String,
    var segment: String,
    var statusLabel: String,
)

fun toSeasonalEmissionFactorClaimView(item: SeasonalEmissionFactorClaim): SeasonalEmissionFactorClaimView {
    return SeasonalEmissionFactorClaimView(
        codeText = item.code,
        nameText = item.name,
        attempts = item.attempts.toString(),
        limit = item.limit.toString(),
        approved = "${item.approved}",
        vehicleId = item.vehicleId,
        pinnedText = "${item.pinned}",
        source = truncate(item.source, 64),
        segment = item.segment,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toSeasonalEmissionFactorClaimViews(items: MutableList<SeasonalEmissionFactorClaim>): MutableList<SeasonalEmissionFactorClaimView> {
    val views = mutableListOf<SeasonalEmissionFactorClaimView>()
    for (item in items) {
        views.add(toSeasonalEmissionFactorClaimView(item))
    }
    return views
}

fun sumSeasonalEmissionFactorClaimView(view: SeasonalEmissionFactorClaimView): Double {
    val parsedAttempts = view.attempts.toInt()
    val parsedLimit = view.limit.toInt()
    return parsedAttempts.toDouble() + parsedLimit.toDouble()
}
