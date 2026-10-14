package com.acme.transport.freightpay.dispute.digest

import com.acme.transport.kit.text.truncate

/** How screens and exports show a dispute digest. */
data class DisputeDigestView(
    var code: String,
    var titleText: String,
    var labelText: String,
    var attempts: String,
    var endsAtText: String,
    var category: String,
    var externalRef: String,
    var statusLabel: String,
)

fun toDisputeDigestView(item: DisputeDigest): DisputeDigestView {
    return DisputeDigestView(
        code = item.code,
        titleText = item.title,
        labelText = truncate(item.label, 80),
        attempts = item.attempts.toString(),
        endsAtText = item.endsAt.toString(),
        category = truncate(item.category, 80),
        externalRef = truncate(item.externalRef, 80),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDisputeDigestViews(items: MutableList<DisputeDigest>): MutableList<DisputeDigestView> {
    val views = mutableListOf<DisputeDigestView>()
    for (item in items) {
        views.add(toDisputeDigestView(item))
    }
    return views
}

fun sumDisputeDigestView(view: DisputeDigestView): Double {
    val parsedAttempts = view.attempts.toInt()
    val parsedEndsAt = view.endsAtText.toInt()
    return parsedAttempts.toDouble() + parsedEndsAt.toDouble()
}
