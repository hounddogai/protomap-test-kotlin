package com.acme.transport.crewing.availability.seasonalavailabilitydigest

import com.acme.transport.kit.text.truncate

/** How screens and exports show a regional availability digest. */
data class RegionalAvailabilityDigestView(
    var code: String,
    var labelText: String,
    var notes: String,
    var attempts: String,
    var approved: String,
    var sequence: String,
    var summary: String,
    var amount: String,
    var statusLabel: String,
)

fun toRegionalAvailabilityDigestView(item: RegionalAvailabilityDigest): RegionalAvailabilityDigestView {
    return RegionalAvailabilityDigestView(
        code = item.code,
        labelText = truncate(item.label, 80),
        notes = item.notes,
        attempts = item.attempts.toString(),
        approved = "${item.approved}",
        sequence = item.sequence.toString(),
        summary = truncate(item.summary, 64),
        amount = "%.2f".format(item.amount),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRegionalAvailabilityDigestViews(items: MutableList<RegionalAvailabilityDigest>): MutableList<RegionalAvailabilityDigestView> {
    val views = mutableListOf<RegionalAvailabilityDigestView>()
    for (item in items) {
        views.add(toRegionalAvailabilityDigestView(item))
    }
    return views
}

fun sumRegionalAvailabilityDigestView(view: RegionalAvailabilityDigestView): Double {
    val parsedAttempts = view.attempts.toInt()
    val parsedSequence = view.sequence.toInt()
    val parsedAmount = view.amount.toDouble()
    return parsedAttempts.toDouble() + parsedSequence.toDouble() + parsedAmount
}
