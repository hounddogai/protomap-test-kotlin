package com.acme.transport.crewing.dutyperiod.target

import com.acme.transport.kit.text.truncate

/** How screens and exports show a strict duty period target. */
data class StrictDutyPeriodTargetView(
    var code: String,
    var name: String,
    var retries: String,
    var createdAt: String,
    var driverIdText: String,
    var statusLabel: String,
)

fun toStrictDutyPeriodTargetView(item: StrictDutyPeriodTarget): StrictDutyPeriodTargetView {
    return StrictDutyPeriodTargetView(
        code = item.code,
        name = item.name,
        retries = item.retries.toString(),
        createdAt = item.createdAt.toString(),
        driverIdText = truncate(item.driverId, 64),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toStrictDutyPeriodTargetViews(items: MutableList<StrictDutyPeriodTarget>): MutableList<StrictDutyPeriodTargetView> {
    val views = mutableListOf<StrictDutyPeriodTargetView>()
    for (item in items) {
        views.add(toStrictDutyPeriodTargetView(item))
    }
    return views
}

fun sumStrictDutyPeriodTargetView(view: StrictDutyPeriodTargetView): Double {
    val parsedRetries = view.retries.toInt()
    val parsedCreatedAt = view.createdAt.toInt()
    return parsedRetries.toDouble() + parsedCreatedAt.toDouble()
}
