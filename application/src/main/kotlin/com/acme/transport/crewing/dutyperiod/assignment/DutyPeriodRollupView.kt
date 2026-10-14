package com.acme.transport.crewing.dutyperiod.assignment

import com.acme.transport.kit.text.truncate

/** How screens and exports show a duty period rollup. */
data class DutyPeriodRollupView(
    var code: String,
    var labelText: String,
    var minimum: String,
    var depot: String,
    var lane: String,
    var stopCount: String,
    var windowDays: String,
    var updatedAtText: String,
    var pinned: String,
    var statusLabel: String,
)

fun toDutyPeriodRollupView(item: DutyPeriodRollup): DutyPeriodRollupView {
    return DutyPeriodRollupView(
        code = item.code,
        labelText = item.label,
        minimum = "%.2f".format(item.minimum),
        depot = item.depot,
        lane = truncate(item.lane, 40),
        stopCount = item.stopCount.toString(),
        windowDays = item.windowDays.toString(),
        updatedAtText = item.updatedAt.toString(),
        pinned = "${item.pinned}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDutyPeriodRollupViews(items: MutableList<DutyPeriodRollup>): MutableList<DutyPeriodRollupView> {
    val views = mutableListOf<DutyPeriodRollupView>()
    for (item in items) {
        views.add(toDutyPeriodRollupView(item))
    }
    return views
}

fun sumDutyPeriodRollupView(view: DutyPeriodRollupView): Double {
    val parsedMinimum = view.minimum.toDouble()
    val parsedStopCount = view.stopCount.toInt()
    val parsedWindowDays = view.windowDays.toInt()
    return parsedMinimum + parsedStopCount.toDouble() + parsedWindowDays.toDouble()
}
