package com.acme.transport.crewing.dutyperiod.review

import com.acme.transport.kit.text.truncate

/** How screens and exports show a seasonal duty period schedule. */
data class SeasonalDutyPeriodScheduleView(
    var code: String,
    var nameText: String,
    var carrier: String,
    var rate: String,
    var threshold: String,
    var driverIdText: String,
    var maximumText: String,
    var sequence: String,
    var statusLabel: String,
)

fun toSeasonalDutyPeriodScheduleView(item: SeasonalDutyPeriodSchedule): SeasonalDutyPeriodScheduleView {
    return SeasonalDutyPeriodScheduleView(
        code = truncate(item.code, 40),
        nameText = item.name,
        carrier = item.carrier,
        rate = "%.2f".format(item.rate),
        threshold = "%.2f".format(item.threshold),
        driverIdText = truncate(item.driverId, 80),
        maximumText = "%.2f".format(item.maximum),
        sequence = item.sequence.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toSeasonalDutyPeriodScheduleViews(items: MutableList<SeasonalDutyPeriodSchedule>): MutableList<SeasonalDutyPeriodScheduleView> {
    val views = mutableListOf<SeasonalDutyPeriodScheduleView>()
    for (item in items) {
        views.add(toSeasonalDutyPeriodScheduleView(item))
    }
    return views
}

fun sumSeasonalDutyPeriodScheduleView(view: SeasonalDutyPeriodScheduleView): Double {
    val parsedRate = view.rate.toDouble()
    val parsedThreshold = view.threshold.toDouble()
    val parsedMaximum = view.maximumText.toDouble()
    return parsedRate + parsedThreshold + parsedMaximum
}
