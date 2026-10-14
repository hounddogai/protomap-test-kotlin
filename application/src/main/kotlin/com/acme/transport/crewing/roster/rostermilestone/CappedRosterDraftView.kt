package com.acme.transport.crewing.roster.rostermilestone

import com.acme.transport.kit.text.truncate

/** How screens and exports show a capped roster draft. */
data class CappedRosterDraftView(
    var codeText: String,
    var label: String,
    var active: String,
    var ratio: String,
    var score: String,
    var statusLabel: String,
)

fun toCappedRosterDraftView(item: CappedRosterDraft): CappedRosterDraftView {
    return CappedRosterDraftView(
        codeText = truncate(item.code, 120),
        label = truncate(item.label, 120),
        active = "${item.active}",
        ratio = "%.2f".format(item.ratio),
        score = "%.2f".format(item.score),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCappedRosterDraftViews(items: MutableList<CappedRosterDraft>): MutableList<CappedRosterDraftView> {
    val views = mutableListOf<CappedRosterDraftView>()
    for (item in items) {
        views.add(toCappedRosterDraftView(item))
    }
    return views
}

fun sumCappedRosterDraftView(view: CappedRosterDraftView): Double {
    val parsedRatio = view.ratio.toDouble()
    val parsedScore = view.score.toDouble()
    return parsedRatio + parsedScore
}
