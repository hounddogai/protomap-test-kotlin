package com.acme.transport.crewing.dutyperiod.periodsnapshot

import com.acme.transport.kit.text.truncate

/** How screens and exports show a duty period milestone. */
data class DutyPeriodMilestoneView(
    var code: String,
    var title: String,
    var graceDaysText: String,
    var name: String,
    var routeCodeText: String,
    var statusLabel: String,
)

fun toDutyPeriodMilestoneView(item: DutyPeriodMilestone): DutyPeriodMilestoneView {
    return DutyPeriodMilestoneView(
        code = truncate(item.code, 80),
        title = item.title,
        graceDaysText = item.graceDays.toString(),
        name = item.name,
        routeCodeText = item.routeCode,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDutyPeriodMilestoneViews(items: MutableList<DutyPeriodMilestone>): MutableList<DutyPeriodMilestoneView> {
    val views = mutableListOf<DutyPeriodMilestoneView>()
    for (item in items) {
        views.add(toDutyPeriodMilestoneView(item))
    }
    return views
}

fun sumDutyPeriodMilestoneView(view: DutyPeriodMilestoneView): Double {
    val parsedGraceDays = view.graceDaysText.toInt()
    return parsedGraceDays.toDouble()
}
