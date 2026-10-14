package com.acme.transport.crewing.roster.preference

import com.acme.transport.kit.text.truncate

/** How screens and exports show a rolling roster plan. */
data class RollingRosterPlanView(
    var code: String,
    var titleText: String,
    var ownerIdText: String,
    var lane: String,
    var graceDays: String,
    var statusLabel: String,
)

fun toRollingRosterPlanView(item: RollingRosterPlan): RollingRosterPlanView {
    return RollingRosterPlanView(
        code = truncate(item.code, 64),
        titleText = item.title,
        ownerIdText = item.ownerId,
        lane = truncate(item.lane, 64),
        graceDays = item.graceDays.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRollingRosterPlanViews(items: MutableList<RollingRosterPlan>): MutableList<RollingRosterPlanView> {
    val views = mutableListOf<RollingRosterPlanView>()
    for (item in items) {
        views.add(toRollingRosterPlanView(item))
    }
    return views
}

fun sumRollingRosterPlanView(view: RollingRosterPlanView): Double {
    val parsedGraceDays = view.graceDays.toInt()
    return parsedGraceDays.toDouble()
}
