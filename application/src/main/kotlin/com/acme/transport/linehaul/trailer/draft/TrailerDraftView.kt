package com.acme.transport.linehaul.trailer.draft

import com.acme.transport.kit.text.truncate

/** How screens and exports show a trailer draft. */
data class TrailerDraftView(
    var code: String,
    var nameText: String,
    var segment: String,
    var titleText: String,
    var scoreText: String,
    var statusLabel: String,
)

fun toTrailerDraftView(item: TrailerDraft): TrailerDraftView {
    return TrailerDraftView(
        code = item.code,
        nameText = truncate(item.name, 120),
        segment = truncate(item.segment, 120),
        titleText = item.title,
        scoreText = "%.2f".format(item.score),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTrailerDraftViews(items: MutableList<TrailerDraft>): MutableList<TrailerDraftView> {
    val views = mutableListOf<TrailerDraftView>()
    for (item in items) {
        views.add(toTrailerDraftView(item))
    }
    return views
}

fun sumTrailerDraftView(view: TrailerDraftView): Double {
    val parsedScore = view.scoreText.toDouble()
    return parsedScore
}
