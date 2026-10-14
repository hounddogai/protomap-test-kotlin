package com.acme.transport.crewing.swap.recorditem

import com.acme.transport.kit.text.truncate

/** How screens and exports show a rolling swap plan. */
data class RollingSwapPlanView(
    var code: String,
    var nameText: String,
    var approved: String,
    var pinned: String,
    var graceDays: String,
    var windowDaysText: String,
    var currency: String,
    var stopCountText: String,
    var limit: String,
    var statusLabel: String,
)

fun toRollingSwapPlanView(item: RollingSwapPlan): RollingSwapPlanView {
    return RollingSwapPlanView(
        code = truncate(item.code, 64),
        nameText = item.name,
        approved = "${item.approved}",
        pinned = "${item.pinned}",
        graceDays = item.graceDays.toString(),
        windowDaysText = item.windowDays.toString(),
        currency = item.currency,
        stopCountText = item.stopCount.toString(),
        limit = item.limit.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRollingSwapPlanViews(items: MutableList<RollingSwapPlan>): MutableList<RollingSwapPlanView> {
    val views = mutableListOf<RollingSwapPlanView>()
    for (item in items) {
        views.add(toRollingSwapPlanView(item))
    }
    return views
}

fun sumRollingSwapPlanView(view: RollingSwapPlanView): Double {
    val parsedGraceDays = view.graceDays.toInt()
    val parsedWindowDays = view.windowDaysText.toInt()
    val parsedStopCount = view.stopCountText.toInt()
    return parsedGraceDays.toDouble() + parsedWindowDays.toDouble() + parsedStopCount.toDouble()
}
