package com.acme.transport.safety.violation.variance

import com.acme.transport.kit.text.truncate

/** How screens and exports show a global violation budget. */
data class GlobalViolationBudgetView(
    var code: String,
    var labelText: String,
    var graceDaysText: String,
    var windowDays: String,
    var startsAt: String,
    var notes: String,
    var active: String,
    var region: String,
    var statusLabel: String,
)

fun toGlobalViolationBudgetView(item: GlobalViolationBudget): GlobalViolationBudgetView {
    return GlobalViolationBudgetView(
        code = item.code,
        labelText = item.label,
        graceDaysText = item.graceDays.toString(),
        windowDays = item.windowDays.toString(),
        startsAt = item.startsAt.toString(),
        notes = truncate(item.notes, 120),
        active = "${item.active}",
        region = item.region,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toGlobalViolationBudgetViews(items: MutableList<GlobalViolationBudget>): MutableList<GlobalViolationBudgetView> {
    val views = mutableListOf<GlobalViolationBudgetView>()
    for (item in items) {
        views.add(toGlobalViolationBudgetView(item))
    }
    return views
}

fun sumGlobalViolationBudgetView(view: GlobalViolationBudgetView): Double {
    val parsedGraceDays = view.graceDaysText.toInt()
    val parsedWindowDays = view.windowDays.toInt()
    val parsedStartsAt = view.startsAt.toInt()
    return parsedGraceDays.toDouble() + parsedWindowDays.toDouble() + parsedStartsAt.toDouble()
}
