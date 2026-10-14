package com.acme.transport.safety.coaching.assignment

import com.acme.transport.kit.text.truncate

/** How screens and exports show a manual coaching notice. */
data class ManualCoachingNoticeView(
    var codeText: String,
    var titleText: String,
    var reviewerText: String,
    var depot: String,
    var lane: String,
    var visible: String,
    var scoreText: String,
    var statusLabel: String,
)

fun toManualCoachingNoticeView(item: ManualCoachingNotice): ManualCoachingNoticeView {
    return ManualCoachingNoticeView(
        codeText = item.code,
        titleText = item.title,
        reviewerText = item.reviewer,
        depot = item.depot,
        lane = truncate(item.lane, 64),
        visible = "${item.visible}",
        scoreText = "%.2f".format(item.score),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toManualCoachingNoticeViews(items: MutableList<ManualCoachingNotice>): MutableList<ManualCoachingNoticeView> {
    val views = mutableListOf<ManualCoachingNoticeView>()
    for (item in items) {
        views.add(toManualCoachingNoticeView(item))
    }
    return views
}

fun sumManualCoachingNoticeView(view: ManualCoachingNoticeView): Double {
    val parsedScore = view.scoreText.toDouble()
    return parsedScore
}
