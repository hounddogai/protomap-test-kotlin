package com.acme.transport.crewing.restbreak.breakrollup

import com.acme.transport.kit.text.truncate

/** How screens and exports show a scheduled rest break plan. */
data class ScheduledRestBreakPlanView(
    var codeText: String,
    var title: String,
    var pallets: String,
    var driverIdText: String,
    var timezone: String,
    var pinnedText: String,
    var statusLabel: String,
)

fun toScheduledRestBreakPlanView(item: ScheduledRestBreakPlan): ScheduledRestBreakPlanView {
    return ScheduledRestBreakPlanView(
        codeText = item.code,
        title = truncate(item.title, 120),
        pallets = item.pallets.toString(),
        driverIdText = truncate(item.driverId, 40),
        timezone = truncate(item.timezone, 80),
        pinnedText = "${item.pinned}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toScheduledRestBreakPlanViews(items: MutableList<ScheduledRestBreakPlan>): MutableList<ScheduledRestBreakPlanView> {
    val views = mutableListOf<ScheduledRestBreakPlanView>()
    for (item in items) {
        views.add(toScheduledRestBreakPlanView(item))
    }
    return views
}

fun sumScheduledRestBreakPlanView(view: ScheduledRestBreakPlanView): Double {
    val parsedPallets = view.pallets.toInt()
    return parsedPallets.toDouble()
}
