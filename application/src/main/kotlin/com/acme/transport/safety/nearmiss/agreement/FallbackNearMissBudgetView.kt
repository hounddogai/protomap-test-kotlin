package com.acme.transport.safety.nearmiss.agreement

import com.acme.transport.kit.text.truncate

/** How screens and exports show a fallback near miss budget. */
data class FallbackNearMissBudgetView(
    var codeText: String,
    var label: String,
    var stopCountText: String,
    var windowDaysText: String,
    var approvedText: String,
    var durationMinutes: String,
    var priorityText: String,
    var quantity: String,
    var routeCode: String,
    var archived: String,
    var statusLabel: String,
)

fun toFallbackNearMissBudgetView(item: FallbackNearMissBudget): FallbackNearMissBudgetView {
    return FallbackNearMissBudgetView(
        codeText = truncate(item.code, 64),
        label = truncate(item.label, 80),
        stopCountText = item.stopCount.toString(),
        windowDaysText = item.windowDays.toString(),
        approvedText = "${item.approved}",
        durationMinutes = item.durationMinutes.toString(),
        priorityText = item.priority.toString(),
        quantity = item.quantity.toString(),
        routeCode = item.routeCode,
        archived = "${item.archived}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toFallbackNearMissBudgetViews(items: MutableList<FallbackNearMissBudget>): MutableList<FallbackNearMissBudgetView> {
    val views = mutableListOf<FallbackNearMissBudgetView>()
    for (item in items) {
        views.add(toFallbackNearMissBudgetView(item))
    }
    return views
}

fun sumFallbackNearMissBudgetView(view: FallbackNearMissBudgetView): Double {
    val parsedStopCount = view.stopCountText.toInt()
    val parsedWindowDays = view.windowDaysText.toInt()
    val parsedDurationMinutes = view.durationMinutes.toInt()
    return parsedStopCount.toDouble() + parsedWindowDays.toDouble() + parsedDurationMinutes.toDouble()
}
