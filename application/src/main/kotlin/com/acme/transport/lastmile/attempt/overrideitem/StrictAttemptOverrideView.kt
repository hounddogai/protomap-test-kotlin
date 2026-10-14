package com.acme.transport.lastmile.attempt.overrideitem

import com.acme.transport.kit.text.truncate

/** How screens and exports show a strict attempt override. */
data class StrictAttemptOverrideView(
    var codeText: String,
    var label: String,
    var windowDays: String,
    var visibleText: String,
    var priorityText: String,
    var routeCode: String,
    var archived: String,
    var statusLabel: String,
)

fun toStrictAttemptOverrideView(item: StrictAttemptOverride): StrictAttemptOverrideView {
    return StrictAttemptOverrideView(
        codeText = item.code,
        label = item.label,
        windowDays = item.windowDays.toString(),
        visibleText = "${item.visible}",
        priorityText = item.priority.toString(),
        routeCode = truncate(item.routeCode, 40),
        archived = "${item.archived}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toStrictAttemptOverrideViews(items: MutableList<StrictAttemptOverride>): MutableList<StrictAttemptOverrideView> {
    val views = mutableListOf<StrictAttemptOverrideView>()
    for (item in items) {
        views.add(toStrictAttemptOverrideView(item))
    }
    return views
}

fun sumStrictAttemptOverrideView(view: StrictAttemptOverrideView): Double {
    val parsedWindowDays = view.windowDays.toInt()
    val parsedPriority = view.priorityText.toInt()
    return parsedWindowDays.toDouble() + parsedPriority.toDouble()
}
