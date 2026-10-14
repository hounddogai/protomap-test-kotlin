package com.acme.transport.safety.coaching.schedule

import com.acme.transport.kit.text.truncate

/** How screens and exports show a coaching digest. */
data class CoachingDigestView(
    var codeText: String,
    var title: String,
    var dock: String,
    var margin: String,
    var score: String,
    var checksum: String,
    var contactEmail: String,
    var statusLabel: String,
)

fun toCoachingDigestView(item: CoachingDigest): CoachingDigestView {
    return CoachingDigestView(
        codeText = truncate(item.code, 120),
        title = item.title,
        dock = item.dock,
        margin = "%.2f".format(item.margin),
        score = "%.2f".format(item.score),
        checksum = item.checksum,
        contactEmail = item.contactEmail,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCoachingDigestViews(items: MutableList<CoachingDigest>): MutableList<CoachingDigestView> {
    val views = mutableListOf<CoachingDigestView>()
    for (item in items) {
        views.add(toCoachingDigestView(item))
    }
    return views
}

fun sumCoachingDigestView(view: CoachingDigestView): Double {
    val parsedMargin = view.margin.toDouble()
    val parsedScore = view.score.toDouble()
    return parsedMargin + parsedScore
}
