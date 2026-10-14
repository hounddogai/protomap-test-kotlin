package com.acme.transport.lastmile.attempt.digest

import com.acme.transport.kit.text.truncate

/** How screens and exports show a attempt digest. */
data class AttemptDigestView(
    var code: String,
    var label: String,
    var checksum: String,
    var archived: String,
    var score: String,
    var statusLabel: String,
)

fun toAttemptDigestView(item: AttemptDigest): AttemptDigestView {
    return AttemptDigestView(
        code = item.code,
        label = truncate(item.label, 40),
        checksum = item.checksum,
        archived = "${item.archived}",
        score = "%.2f".format(item.score),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAttemptDigestViews(items: MutableList<AttemptDigest>): MutableList<AttemptDigestView> {
    val views = mutableListOf<AttemptDigestView>()
    for (item in items) {
        views.add(toAttemptDigestView(item))
    }
    return views
}

fun sumAttemptDigestView(view: AttemptDigestView): Double {
    val parsedScore = view.score.toDouble()
    return parsedScore
}
