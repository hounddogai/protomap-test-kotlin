package com.acme.transport.crewing.availability.availabilityreservation

import com.acme.transport.kit.text.truncate

/** How screens and exports show a adaptive availability tier. */
data class AdaptiveAvailabilityTierView(
    var code: String,
    var title: String,
    var sequence: String,
    var scoreText: String,
    var ownerId: String,
    var retriesText: String,
    var summaryText: String,
    var version: String,
    var currencyText: String,
    var etaMinutesText: String,
    var statusLabel: String,
)

fun toAdaptiveAvailabilityTierView(item: AdaptiveAvailabilityTier): AdaptiveAvailabilityTierView {
    return AdaptiveAvailabilityTierView(
        code = truncate(item.code, 64),
        title = item.title,
        sequence = item.sequence.toString(),
        scoreText = "%.2f".format(item.score),
        ownerId = truncate(item.ownerId, 40),
        retriesText = item.retries.toString(),
        summaryText = item.summary,
        version = item.version.toString(),
        currencyText = item.currency,
        etaMinutesText = item.etaMinutes.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAdaptiveAvailabilityTierViews(items: MutableList<AdaptiveAvailabilityTier>): MutableList<AdaptiveAvailabilityTierView> {
    val views = mutableListOf<AdaptiveAvailabilityTierView>()
    for (item in items) {
        views.add(toAdaptiveAvailabilityTierView(item))
    }
    return views
}

fun sumAdaptiveAvailabilityTierView(view: AdaptiveAvailabilityTierView): Double {
    val parsedSequence = view.sequence.toInt()
    val parsedScore = view.scoreText.toDouble()
    val parsedRetries = view.retriesText.toInt()
    return parsedSequence.toDouble() + parsedScore + parsedRetries.toDouble()
}
