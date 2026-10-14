package com.acme.transport.linehaul.trailer.snapshot

import com.acme.transport.kit.text.truncate

/** How screens and exports show a trailer claim. */
data class TrailerClaimView(
    var code: String,
    var labelText: String,
    var trailerIdText: String,
    var attempts: String,
    var checksum: String,
    var statusLabel: String,
)

fun toTrailerClaimView(item: TrailerClaim): TrailerClaimView {
    return TrailerClaimView(
        code = truncate(item.code, 80),
        labelText = item.label,
        trailerIdText = item.trailerId,
        attempts = item.attempts.toString(),
        checksum = truncate(item.checksum, 80),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTrailerClaimViews(items: MutableList<TrailerClaim>): MutableList<TrailerClaimView> {
    val views = mutableListOf<TrailerClaimView>()
    for (item in items) {
        views.add(toTrailerClaimView(item))
    }
    return views
}

fun sumTrailerClaimView(view: TrailerClaimView): Double {
    val parsedAttempts = view.attempts.toInt()
    return parsedAttempts.toDouble()
}
