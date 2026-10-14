package com.acme.transport.terminal.traileryard.tier

import com.acme.transport.kit.text.truncate

/** How screens and exports show a trailer yard claim. */
data class TrailerYardClaimView(
    var code: String,
    var name: String,
    var limit: String,
    var reviewerText: String,
    var loadKg: String,
    var externalRef: String,
    var lane: String,
    var weight: String,
    var statusLabel: String,
)

fun toTrailerYardClaimView(item: TrailerYardClaim): TrailerYardClaimView {
    return TrailerYardClaimView(
        code = item.code,
        name = truncate(item.name, 40),
        limit = item.limit.toString(),
        reviewerText = item.reviewer,
        loadKg = "%.2f".format(item.loadKg),
        externalRef = item.externalRef,
        lane = item.lane,
        weight = "%.2f".format(item.weight),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTrailerYardClaimViews(items: MutableList<TrailerYardClaim>): MutableList<TrailerYardClaimView> {
    val views = mutableListOf<TrailerYardClaimView>()
    for (item in items) {
        views.add(toTrailerYardClaimView(item))
    }
    return views
}

fun sumTrailerYardClaimView(view: TrailerYardClaimView): Double {
    val parsedLimit = view.limit.toInt()
    val parsedLoadKg = view.loadKg.toDouble()
    val parsedWeight = view.weight.toDouble()
    return parsedLimit.toDouble() + parsedLoadKg + parsedWeight
}
