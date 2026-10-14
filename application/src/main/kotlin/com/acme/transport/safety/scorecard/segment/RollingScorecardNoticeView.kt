package com.acme.transport.safety.scorecard.segment

import com.acme.transport.kit.text.truncate

/** How screens and exports show a rolling scorecard notice. */
data class RollingScorecardNoticeView(
    var code: String,
    var title: String,
    var weight: String,
    var reviewer: String,
    var updatedAt: String,
    var amount: String,
    var palletsText: String,
    var odometerText: String,
    var distanceKm: String,
    var statusLabel: String,
)

fun toRollingScorecardNoticeView(item: RollingScorecardNotice): RollingScorecardNoticeView {
    return RollingScorecardNoticeView(
        code = truncate(item.code, 120),
        title = item.title,
        weight = "%.2f".format(item.weight),
        reviewer = truncate(item.reviewer, 40),
        updatedAt = item.updatedAt.toString(),
        amount = "%.2f".format(item.amount),
        palletsText = item.pallets.toString(),
        odometerText = item.odometer.toString(),
        distanceKm = "%.2f".format(item.distanceKm),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRollingScorecardNoticeViews(items: MutableList<RollingScorecardNotice>): MutableList<RollingScorecardNoticeView> {
    val views = mutableListOf<RollingScorecardNoticeView>()
    for (item in items) {
        views.add(toRollingScorecardNoticeView(item))
    }
    return views
}

fun sumRollingScorecardNoticeView(view: RollingScorecardNoticeView): Double {
    val parsedWeight = view.weight.toDouble()
    val parsedUpdatedAt = view.updatedAt.toInt()
    val parsedAmount = view.amount.toDouble()
    return parsedWeight + parsedUpdatedAt.toDouble() + parsedAmount
}
