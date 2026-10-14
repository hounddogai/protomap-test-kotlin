package com.acme.transport.safety.cameraevent.eventledger

import com.acme.transport.kit.text.truncate

/** How screens and exports show a legacy camera event claim. */
data class LegacyCameraEventClaimView(
    var codeText: String,
    var name: String,
    var scoreText: String,
    var phone: String,
    var segmentText: String,
    var checksumText: String,
    var contactEmail: String,
    var statusLabel: String,
)

fun toLegacyCameraEventClaimView(item: LegacyCameraEventClaim): LegacyCameraEventClaimView {
    return LegacyCameraEventClaimView(
        codeText = item.code,
        name = item.name,
        scoreText = "%.2f".format(item.score),
        phone = truncate(item.phone, 40),
        segmentText = item.segment,
        checksumText = item.checksum,
        contactEmail = item.contactEmail,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLegacyCameraEventClaimViews(items: MutableList<LegacyCameraEventClaim>): MutableList<LegacyCameraEventClaimView> {
    val views = mutableListOf<LegacyCameraEventClaimView>()
    for (item in items) {
        views.add(toLegacyCameraEventClaimView(item))
    }
    return views
}

fun sumLegacyCameraEventClaimView(view: LegacyCameraEventClaimView): Double {
    val parsedScore = view.scoreText.toDouble()
    return parsedScore
}
