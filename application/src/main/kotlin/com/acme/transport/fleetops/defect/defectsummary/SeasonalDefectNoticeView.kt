package com.acme.transport.fleetops.defect.defectsummary

import com.acme.transport.kit.text.truncate

/** How screens and exports show a seasonal defect notice. */
data class SeasonalDefectNoticeView(
    var code: String,
    var name: String,
    var score: String,
    var discount: String,
    var active: String,
    var attempts: String,
    var statusLabel: String,
)

fun toSeasonalDefectNoticeView(item: SeasonalDefectNotice): SeasonalDefectNoticeView {
    return SeasonalDefectNoticeView(
        code = truncate(item.code, 40),
        name = item.name,
        score = "%.2f".format(item.score),
        discount = "%.2f".format(item.discount),
        active = "${item.active}",
        attempts = item.attempts.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toSeasonalDefectNoticeViews(items: MutableList<SeasonalDefectNotice>): MutableList<SeasonalDefectNoticeView> {
    val views = mutableListOf<SeasonalDefectNoticeView>()
    for (item in items) {
        views.add(toSeasonalDefectNoticeView(item))
    }
    return views
}

fun sumSeasonalDefectNoticeView(view: SeasonalDefectNoticeView): Double {
    val parsedScore = view.score.toDouble()
    val parsedDiscount = view.discount.toDouble()
    val parsedAttempts = view.attempts.toInt()
    return parsedScore + parsedDiscount + parsedAttempts.toDouble()
}
