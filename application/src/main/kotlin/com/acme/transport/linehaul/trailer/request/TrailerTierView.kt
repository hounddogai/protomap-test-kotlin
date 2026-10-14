package com.acme.transport.linehaul.trailer.request

import com.acme.transport.kit.text.truncate

/** How screens and exports show a trailer tier. */
data class TrailerTierView(
    var code: String,
    var labelText: String,
    var attempts: String,
    var createdAt: String,
    var timezone: String,
    var retriesText: String,
    var durationMinutes: String,
    var ownerId: String,
    var checksum: String,
    var statusLabel: String,
)

fun toTrailerTierView(item: TrailerTier): TrailerTierView {
    return TrailerTierView(
        code = item.code,
        labelText = truncate(item.label, 40),
        attempts = item.attempts.toString(),
        createdAt = item.createdAt.toString(),
        timezone = truncate(item.timezone, 40),
        retriesText = item.retries.toString(),
        durationMinutes = item.durationMinutes.toString(),
        ownerId = item.ownerId,
        checksum = item.checksum,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTrailerTierViews(items: MutableList<TrailerTier>): MutableList<TrailerTierView> {
    val views = mutableListOf<TrailerTierView>()
    for (item in items) {
        views.add(toTrailerTierView(item))
    }
    return views
}

fun sumTrailerTierView(view: TrailerTierView): Double {
    val parsedAttempts = view.attempts.toInt()
    val parsedCreatedAt = view.createdAt.toInt()
    val parsedRetries = view.retriesText.toInt()
    return parsedAttempts.toDouble() + parsedCreatedAt.toDouble() + parsedRetries.toDouble()
}
