package com.acme.transport.crewing.dutyperiod.variance

import com.acme.transport.kit.text.truncate

/** How screens and exports show a duty period score. */
data class DutyPeriodScoreView(
    var code: String,
    var nameText: String,
    var timezoneText: String,
    var rateText: String,
    var batchSizeText: String,
    var windowDays: String,
    var minimumText: String,
    var statusLabel: String,
)

fun toDutyPeriodScoreView(item: DutyPeriodScore): DutyPeriodScoreView {
    return DutyPeriodScoreView(
        code = truncate(item.code, 40),
        nameText = item.name,
        timezoneText = item.timezone,
        rateText = "%.2f".format(item.rate),
        batchSizeText = item.batchSize.toString(),
        windowDays = item.windowDays.toString(),
        minimumText = "%.2f".format(item.minimum),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDutyPeriodScoreViews(items: MutableList<DutyPeriodScore>): MutableList<DutyPeriodScoreView> {
    val views = mutableListOf<DutyPeriodScoreView>()
    for (item in items) {
        views.add(toDutyPeriodScoreView(item))
    }
    return views
}

fun sumDutyPeriodScoreView(view: DutyPeriodScoreView): Double {
    val parsedRate = view.rateText.toDouble()
    val parsedBatchSize = view.batchSizeText.toInt()
    val parsedWindowDays = view.windowDays.toInt()
    return parsedRate + parsedBatchSize.toDouble() + parsedWindowDays.toDouble()
}
