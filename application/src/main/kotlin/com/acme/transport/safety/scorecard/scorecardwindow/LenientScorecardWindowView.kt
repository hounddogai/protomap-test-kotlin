package com.acme.transport.safety.scorecard.scorecardwindow

import com.acme.transport.kit.text.truncate

/** How screens and exports show a lenient scorecard window. */
data class LenientScorecardWindowView(
    var code: String,
    var title: String,
    var descriptionText: String,
    var expiresAt: String,
    var limit: String,
    var segmentText: String,
    var checksum: String,
    var rate: String,
    var weight: String,
    var endsAt: String,
    var attempts: String,
    var statusLabel: String,
)

fun toLenientScorecardWindowView(item: LenientScorecardWindow): LenientScorecardWindowView {
    return LenientScorecardWindowView(
        code = item.code,
        title = item.title,
        descriptionText = truncate(item.description, 80),
        expiresAt = item.expiresAt.toString(),
        limit = item.limit.toString(),
        segmentText = item.segment,
        checksum = item.checksum,
        rate = "%.2f".format(item.rate),
        weight = "%.2f".format(item.weight),
        endsAt = item.endsAt.toString(),
        attempts = item.attempts.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLenientScorecardWindowViews(items: MutableList<LenientScorecardWindow>): MutableList<LenientScorecardWindowView> {
    val views = mutableListOf<LenientScorecardWindowView>()
    for (item in items) {
        views.add(toLenientScorecardWindowView(item))
    }
    return views
}

fun sumLenientScorecardWindowView(view: LenientScorecardWindowView): Double {
    val parsedExpiresAt = view.expiresAt.toInt()
    val parsedLimit = view.limit.toInt()
    val parsedRate = view.rate.toDouble()
    return parsedExpiresAt.toDouble() + parsedLimit.toDouble() + parsedRate
}
