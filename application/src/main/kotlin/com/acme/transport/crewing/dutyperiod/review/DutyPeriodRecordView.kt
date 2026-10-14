package com.acme.transport.crewing.dutyperiod.review

import com.acme.transport.kit.text.truncate

/** How screens and exports show a duty period record. */
data class DutyPeriodRecordView(
    var code: String,
    var name: String,
    var category: String,
    var limit: String,
    var routeCode: String,
    var expiresAt: String,
    var archived: String,
    var windowDaysText: String,
    var driverId: String,
    var graceDays: String,
    var statusLabel: String,
)

fun toDutyPeriodRecordView(item: DutyPeriodRecord): DutyPeriodRecordView {
    return DutyPeriodRecordView(
        code = item.code,
        name = item.name,
        category = item.category,
        limit = item.limit.toString(),
        routeCode = truncate(item.routeCode, 64),
        expiresAt = item.expiresAt.toString(),
        archived = "${item.archived}",
        windowDaysText = item.windowDays.toString(),
        driverId = item.driverId,
        graceDays = item.graceDays.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDutyPeriodRecordViews(items: MutableList<DutyPeriodRecord>): MutableList<DutyPeriodRecordView> {
    val views = mutableListOf<DutyPeriodRecordView>()
    for (item in items) {
        views.add(toDutyPeriodRecordView(item))
    }
    return views
}

fun sumDutyPeriodRecordView(view: DutyPeriodRecordView): Double {
    val parsedLimit = view.limit.toInt()
    val parsedExpiresAt = view.expiresAt.toInt()
    val parsedWindowDays = view.windowDaysText.toInt()
    return parsedLimit.toDouble() + parsedExpiresAt.toDouble() + parsedWindowDays.toDouble()
}
