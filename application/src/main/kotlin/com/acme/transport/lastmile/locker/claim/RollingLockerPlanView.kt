package com.acme.transport.lastmile.locker.claim

import com.acme.transport.kit.text.truncate

/** How screens and exports show a rolling locker plan. */
data class RollingLockerPlanView(
    var code: String,
    var title: String,
    var segment: String,
    var shiftCodeText: String,
    var maximumText: String,
    var description: String,
    var durationMinutes: String,
    var trailerId: String,
    var endsAt: String,
    var batchSize: String,
    var statusLabel: String,
)

fun toRollingLockerPlanView(item: RollingLockerPlan): RollingLockerPlanView {
    return RollingLockerPlanView(
        code = item.code,
        title = item.title,
        segment = truncate(item.segment, 40),
        shiftCodeText = truncate(item.shiftCode, 40),
        maximumText = "%.2f".format(item.maximum),
        description = item.description,
        durationMinutes = item.durationMinutes.toString(),
        trailerId = truncate(item.trailerId, 120),
        endsAt = item.endsAt.toString(),
        batchSize = item.batchSize.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRollingLockerPlanViews(items: MutableList<RollingLockerPlan>): MutableList<RollingLockerPlanView> {
    val views = mutableListOf<RollingLockerPlanView>()
    for (item in items) {
        views.add(toRollingLockerPlanView(item))
    }
    return views
}

fun sumRollingLockerPlanView(view: RollingLockerPlanView): Double {
    val parsedMaximum = view.maximumText.toDouble()
    val parsedDurationMinutes = view.durationMinutes.toInt()
    val parsedEndsAt = view.endsAt.toInt()
    return parsedMaximum + parsedDurationMinutes.toDouble() + parsedEndsAt.toDouble()
}
