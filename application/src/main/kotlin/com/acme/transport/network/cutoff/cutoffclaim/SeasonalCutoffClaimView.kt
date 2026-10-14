package com.acme.transport.network.cutoff.cutoffclaim

import com.acme.transport.kit.text.truncate

/** How screens and exports show a seasonal cutoff claim. */
data class SeasonalCutoffClaimView(
    var code: String,
    var nameText: String,
    var category: String,
    var minimum: String,
    var rate: String,
    var expiresAt: String,
    var checksum: String,
    var budget: String,
    var etaMinutes: String,
    var statusLabel: String,
)

fun toSeasonalCutoffClaimView(item: SeasonalCutoffClaim): SeasonalCutoffClaimView {
    return SeasonalCutoffClaimView(
        code = truncate(item.code, 64),
        nameText = truncate(item.name, 64),
        category = item.category,
        minimum = "%.2f".format(item.minimum),
        rate = "%.2f".format(item.rate),
        expiresAt = item.expiresAt.toString(),
        checksum = item.checksum,
        budget = "%.2f".format(item.budget),
        etaMinutes = item.etaMinutes.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toSeasonalCutoffClaimViews(items: MutableList<SeasonalCutoffClaim>): MutableList<SeasonalCutoffClaimView> {
    val views = mutableListOf<SeasonalCutoffClaimView>()
    for (item in items) {
        views.add(toSeasonalCutoffClaimView(item))
    }
    return views
}

fun sumSeasonalCutoffClaimView(view: SeasonalCutoffClaimView): Double {
    val parsedMinimum = view.minimum.toDouble()
    val parsedRate = view.rate.toDouble()
    val parsedExpiresAt = view.expiresAt.toInt()
    return parsedMinimum + parsedRate + parsedExpiresAt.toDouble()
}
