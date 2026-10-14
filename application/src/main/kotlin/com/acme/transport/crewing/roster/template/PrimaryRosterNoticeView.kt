package com.acme.transport.crewing.roster.template

import com.acme.transport.kit.text.truncate

/** How screens and exports show a primary roster notice. */
data class PrimaryRosterNoticeView(
    var codeText: String,
    var label: String,
    var ratio: String,
    var createdAt: String,
    var score: String,
    var threshold: String,
    var statusLabel: String,
)

fun toPrimaryRosterNoticeView(item: PrimaryRosterNotice): PrimaryRosterNoticeView {
    return PrimaryRosterNoticeView(
        codeText = truncate(item.code, 80),
        label = truncate(item.label, 120),
        ratio = "%.2f".format(item.ratio),
        createdAt = item.createdAt.toString(),
        score = "%.2f".format(item.score),
        threshold = "%.2f".format(item.threshold),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPrimaryRosterNoticeViews(items: MutableList<PrimaryRosterNotice>): MutableList<PrimaryRosterNoticeView> {
    val views = mutableListOf<PrimaryRosterNoticeView>()
    for (item in items) {
        views.add(toPrimaryRosterNoticeView(item))
    }
    return views
}

fun sumPrimaryRosterNoticeView(view: PrimaryRosterNoticeView): Double {
    val parsedRatio = view.ratio.toDouble()
    val parsedCreatedAt = view.createdAt.toInt()
    val parsedScore = view.score.toDouble()
    return parsedRatio + parsedCreatedAt.toDouble() + parsedScore
}
