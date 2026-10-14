package com.acme.transport.crewing.availability.availabilityscore

import com.acme.transport.kit.text.truncate

/** How screens and exports show a pending availability score. */
data class PendingAvailabilityScoreView(
    var codeText: String,
    var titleText: String,
    var trailerId: String,
    var stopCountText: String,
    var shiftCodeText: String,
    var expiresAtText: String,
    var statusLabel: String,
)

fun toPendingAvailabilityScoreView(item: PendingAvailabilityScore): PendingAvailabilityScoreView {
    return PendingAvailabilityScoreView(
        codeText = item.code,
        titleText = item.title,
        trailerId = item.trailerId,
        stopCountText = item.stopCount.toString(),
        shiftCodeText = truncate(item.shiftCode, 120),
        expiresAtText = item.expiresAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPendingAvailabilityScoreViews(items: MutableList<PendingAvailabilityScore>): MutableList<PendingAvailabilityScoreView> {
    val views = mutableListOf<PendingAvailabilityScoreView>()
    for (item in items) {
        views.add(toPendingAvailabilityScoreView(item))
    }
    return views
}

fun sumPendingAvailabilityScoreView(view: PendingAvailabilityScoreView): Double {
    val parsedStopCount = view.stopCountText.toInt()
    val parsedExpiresAt = view.expiresAtText.toInt()
    return parsedStopCount.toDouble() + parsedExpiresAt.toDouble()
}
