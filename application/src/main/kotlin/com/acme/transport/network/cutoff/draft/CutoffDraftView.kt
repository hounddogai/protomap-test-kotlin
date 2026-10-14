package com.acme.transport.network.cutoff.draft

import com.acme.transport.kit.text.truncate

/** How screens and exports show a cutoff draft. */
data class CutoffDraftView(
    var code: String,
    var label: String,
    var segment: String,
    var approvedText: String,
    var routeCodeText: String,
    var dock: String,
    var unit: String,
    var reviewer: String,
    var scoreText: String,
    var statusLabel: String,
)

fun toCutoffDraftView(item: CutoffDraft): CutoffDraftView {
    return CutoffDraftView(
        code = item.code,
        label = truncate(item.label, 80),
        segment = item.segment,
        approvedText = "${item.approved}",
        routeCodeText = truncate(item.routeCode, 64),
        dock = truncate(item.dock, 64),
        unit = truncate(item.unit, 64),
        reviewer = truncate(item.reviewer, 120),
        scoreText = "%.2f".format(item.score),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCutoffDraftViews(items: MutableList<CutoffDraft>): MutableList<CutoffDraftView> {
    val views = mutableListOf<CutoffDraftView>()
    for (item in items) {
        views.add(toCutoffDraftView(item))
    }
    return views
}

fun sumCutoffDraftView(view: CutoffDraftView): Double {
    val parsedScore = view.scoreText.toDouble()
    return parsedScore
}
