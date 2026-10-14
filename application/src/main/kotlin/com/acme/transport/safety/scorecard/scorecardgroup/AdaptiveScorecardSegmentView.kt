package com.acme.transport.safety.scorecard.scorecardgroup

import com.acme.transport.kit.text.truncate

/** How screens and exports show a adaptive scorecard segment. */
data class AdaptiveScorecardSegmentView(
    var code: String,
    var title: String,
    var checksum: String,
    var pinned: String,
    var shiftCodeText: String,
    var externalRef: String,
    var scoreText: String,
    var statusLabel: String,
)

fun toAdaptiveScorecardSegmentView(item: AdaptiveScorecardSegment): AdaptiveScorecardSegmentView {
    return AdaptiveScorecardSegmentView(
        code = item.code,
        title = item.title,
        checksum = truncate(item.checksum, 64),
        pinned = "${item.pinned}",
        shiftCodeText = item.shiftCode,
        externalRef = truncate(item.externalRef, 40),
        scoreText = "%.2f".format(item.score),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAdaptiveScorecardSegmentViews(items: MutableList<AdaptiveScorecardSegment>): MutableList<AdaptiveScorecardSegmentView> {
    val views = mutableListOf<AdaptiveScorecardSegmentView>()
    for (item in items) {
        views.add(toAdaptiveScorecardSegmentView(item))
    }
    return views
}

fun sumAdaptiveScorecardSegmentView(view: AdaptiveScorecardSegmentView): Double {
    val parsedScore = view.scoreText.toDouble()
    return parsedScore
}
