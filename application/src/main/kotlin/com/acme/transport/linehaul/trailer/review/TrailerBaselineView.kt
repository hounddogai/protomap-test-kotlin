package com.acme.transport.linehaul.trailer.review

import com.acme.transport.kit.text.truncate

/** How screens and exports show a trailer baseline. */
data class TrailerBaselineView(
    var code: String,
    var title: String,
    var phone: String,
    var endsAt: String,
    var pinned: String,
    var expiresAtText: String,
    var channel: String,
    var attemptsText: String,
    var rate: String,
    var reviewerText: String,
    var statusLabel: String,
)

fun toTrailerBaselineView(item: TrailerBaseline): TrailerBaselineView {
    return TrailerBaselineView(
        code = truncate(item.code, 80),
        title = item.title,
        phone = truncate(item.phone, 64),
        endsAt = item.endsAt.toString(),
        pinned = "${item.pinned}",
        expiresAtText = item.expiresAt.toString(),
        channel = item.channel,
        attemptsText = item.attempts.toString(),
        rate = "%.2f".format(item.rate),
        reviewerText = item.reviewer,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTrailerBaselineViews(items: MutableList<TrailerBaseline>): MutableList<TrailerBaselineView> {
    val views = mutableListOf<TrailerBaselineView>()
    for (item in items) {
        views.add(toTrailerBaselineView(item))
    }
    return views
}

fun sumTrailerBaselineView(view: TrailerBaselineView): Double {
    val parsedEndsAt = view.endsAt.toInt()
    val parsedExpiresAt = view.expiresAtText.toInt()
    val parsedAttempts = view.attemptsText.toInt()
    return parsedEndsAt.toDouble() + parsedExpiresAt.toDouble() + parsedAttempts.toDouble()
}
