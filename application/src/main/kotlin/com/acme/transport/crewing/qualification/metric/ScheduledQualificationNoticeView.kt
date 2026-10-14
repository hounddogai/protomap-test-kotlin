package com.acme.transport.crewing.qualification.metric

import com.acme.transport.kit.text.truncate

/** How screens and exports show a scheduled qualification notice. */
data class ScheduledQualificationNoticeView(
    var code: String,
    var titleText: String,
    var category: String,
    var score: String,
    var thresholdText: String,
    var expiresAt: String,
    var etaMinutesText: String,
    var shiftCode: String,
    var endsAt: String,
    var statusLabel: String,
)

fun toScheduledQualificationNoticeView(item: ScheduledQualificationNotice): ScheduledQualificationNoticeView {
    return ScheduledQualificationNoticeView(
        code = item.code,
        titleText = item.title,
        category = truncate(item.category, 40),
        score = "%.2f".format(item.score),
        thresholdText = "%.2f".format(item.threshold),
        expiresAt = item.expiresAt.toString(),
        etaMinutesText = item.etaMinutes.toString(),
        shiftCode = item.shiftCode,
        endsAt = item.endsAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toScheduledQualificationNoticeViews(items: MutableList<ScheduledQualificationNotice>): MutableList<ScheduledQualificationNoticeView> {
    val views = mutableListOf<ScheduledQualificationNoticeView>()
    for (item in items) {
        views.add(toScheduledQualificationNoticeView(item))
    }
    return views
}

fun sumScheduledQualificationNoticeView(view: ScheduledQualificationNoticeView): Double {
    val parsedScore = view.score.toDouble()
    val parsedThreshold = view.thresholdText.toDouble()
    val parsedExpiresAt = view.expiresAt.toInt()
    return parsedScore + parsedThreshold + parsedExpiresAt.toDouble()
}
