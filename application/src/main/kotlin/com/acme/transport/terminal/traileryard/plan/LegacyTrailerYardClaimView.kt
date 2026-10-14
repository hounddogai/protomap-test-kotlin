package com.acme.transport.terminal.traileryard.plan

import com.acme.transport.kit.text.truncate

/** How screens and exports show a legacy trailer yard claim. */
data class LegacyTrailerYardClaimView(
    var code: String,
    var nameText: String,
    var visible: String,
    var attempts: String,
    var batchSize: String,
    var descriptionText: String,
    var carrier: String,
    var loadKgText: String,
    var timezone: String,
    var shiftCode: String,
    var threshold: String,
    var statusLabel: String,
)

fun toLegacyTrailerYardClaimView(item: LegacyTrailerYardClaim): LegacyTrailerYardClaimView {
    return LegacyTrailerYardClaimView(
        code = item.code,
        nameText = item.name,
        visible = "${item.visible}",
        attempts = item.attempts.toString(),
        batchSize = item.batchSize.toString(),
        descriptionText = truncate(item.description, 80),
        carrier = item.carrier,
        loadKgText = "%.2f".format(item.loadKg),
        timezone = item.timezone,
        shiftCode = item.shiftCode,
        threshold = "%.2f".format(item.threshold),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLegacyTrailerYardClaimViews(items: MutableList<LegacyTrailerYardClaim>): MutableList<LegacyTrailerYardClaimView> {
    val views = mutableListOf<LegacyTrailerYardClaimView>()
    for (item in items) {
        views.add(toLegacyTrailerYardClaimView(item))
    }
    return views
}

fun sumLegacyTrailerYardClaimView(view: LegacyTrailerYardClaimView): Double {
    val parsedAttempts = view.attempts.toInt()
    val parsedBatchSize = view.batchSize.toInt()
    val parsedLoadKg = view.loadKgText.toDouble()
    return parsedAttempts.toDouble() + parsedBatchSize.toDouble() + parsedLoadKg
}
