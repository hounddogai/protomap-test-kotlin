package com.acme.transport.terminal.crossdock.policy

import com.acme.transport.kit.text.truncate

/** How screens and exports show a default cross dock schedule. */
data class DefaultCrossDockScheduleView(
    var code: String,
    var title: String,
    var routeCode: String,
    var budget: String,
    var checksum: String,
    var cost: String,
    var priority: String,
    var summaryText: String,
    var loadKg: String,
    var statusLabel: String,
)

fun toDefaultCrossDockScheduleView(item: DefaultCrossDockSchedule): DefaultCrossDockScheduleView {
    return DefaultCrossDockScheduleView(
        code = item.code,
        title = truncate(item.title, 120),
        routeCode = item.routeCode,
        budget = "%.2f".format(item.budget),
        checksum = truncate(item.checksum, 64),
        cost = "%.2f".format(item.cost),
        priority = item.priority.toString(),
        summaryText = item.summary,
        loadKg = "%.2f".format(item.loadKg),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDefaultCrossDockScheduleViews(items: MutableList<DefaultCrossDockSchedule>): MutableList<DefaultCrossDockScheduleView> {
    val views = mutableListOf<DefaultCrossDockScheduleView>()
    for (item in items) {
        views.add(toDefaultCrossDockScheduleView(item))
    }
    return views
}

fun sumDefaultCrossDockScheduleView(view: DefaultCrossDockScheduleView): Double {
    val parsedBudget = view.budget.toDouble()
    val parsedCost = view.cost.toDouble()
    val parsedPriority = view.priority.toInt()
    return parsedBudget + parsedCost + parsedPriority.toDouble()
}
