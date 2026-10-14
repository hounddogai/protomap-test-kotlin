package com.acme.transport.crewing.dutyperiod.policy

import com.acme.transport.kit.text.truncate

/** How screens and exports show a duty period batch. */
data class DutyPeriodBatchView(
    var code: String,
    var title: String,
    var costText: String,
    var minimumText: String,
    var reviewer: String,
    var checksum: String,
    var odometer: String,
    var locale: String,
    var statusLabel: String,
)

fun toDutyPeriodBatchView(item: DutyPeriodBatch): DutyPeriodBatchView {
    return DutyPeriodBatchView(
        code = item.code,
        title = truncate(item.title, 64),
        costText = "%.2f".format(item.cost),
        minimumText = "%.2f".format(item.minimum),
        reviewer = truncate(item.reviewer, 40),
        checksum = item.checksum,
        odometer = item.odometer.toString(),
        locale = item.locale,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDutyPeriodBatchViews(items: MutableList<DutyPeriodBatch>): MutableList<DutyPeriodBatchView> {
    val views = mutableListOf<DutyPeriodBatchView>()
    for (item in items) {
        views.add(toDutyPeriodBatchView(item))
    }
    return views
}

fun sumDutyPeriodBatchView(view: DutyPeriodBatchView): Double {
    val parsedCost = view.costText.toDouble()
    val parsedMinimum = view.minimumText.toDouble()
    val parsedOdometer = view.odometer.toInt()
    return parsedCost + parsedMinimum + parsedOdometer.toDouble()
}
