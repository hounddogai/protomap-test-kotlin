package com.acme.transport.safety.violation.manualviolationquota

import com.acme.transport.kit.text.truncate

/** How screens and exports show a fallback violation slot. */
data class FallbackViolationSlotView(
    var code: String,
    var title: String,
    var localeText: String,
    var ceiling: String,
    var priority: String,
    var reviewer: String,
    var windowDaysText: String,
    var statusLabel: String,
)

fun toFallbackViolationSlotView(item: FallbackViolationSlot): FallbackViolationSlotView {
    return FallbackViolationSlotView(
        code = item.code,
        title = truncate(item.title, 120),
        localeText = item.locale,
        ceiling = "%.2f".format(item.ceiling),
        priority = item.priority.toString(),
        reviewer = item.reviewer,
        windowDaysText = item.windowDays.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toFallbackViolationSlotViews(items: MutableList<FallbackViolationSlot>): MutableList<FallbackViolationSlotView> {
    val views = mutableListOf<FallbackViolationSlotView>()
    for (item in items) {
        views.add(toFallbackViolationSlotView(item))
    }
    return views
}

fun sumFallbackViolationSlotView(view: FallbackViolationSlotView): Double {
    val parsedCeiling = view.ceiling.toDouble()
    val parsedPriority = view.priority.toInt()
    val parsedWindowDays = view.windowDaysText.toInt()
    return parsedCeiling + parsedPriority.toDouble() + parsedWindowDays.toDouble()
}
