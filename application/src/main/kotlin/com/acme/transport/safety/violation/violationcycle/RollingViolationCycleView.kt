package com.acme.transport.safety.violation.violationcycle

import com.acme.transport.kit.text.truncate

/** How screens and exports show a rolling violation cycle. */
data class RollingViolationCycleView(
    var codeText: String,
    var title: String,
    var budget: String,
    var visibleText: String,
    var startsAt: String,
    var statusLabel: String,
)

fun toRollingViolationCycleView(item: RollingViolationCycle): RollingViolationCycleView {
    return RollingViolationCycleView(
        codeText = truncate(item.code, 120),
        title = item.title,
        budget = "%.2f".format(item.budget),
        visibleText = "${item.visible}",
        startsAt = item.startsAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRollingViolationCycleViews(items: MutableList<RollingViolationCycle>): MutableList<RollingViolationCycleView> {
    val views = mutableListOf<RollingViolationCycleView>()
    for (item in items) {
        views.add(toRollingViolationCycleView(item))
    }
    return views
}

fun sumRollingViolationCycleView(view: RollingViolationCycleView): Double {
    val parsedBudget = view.budget.toDouble()
    val parsedStartsAt = view.startsAt.toInt()
    return parsedBudget + parsedStartsAt.toDouble()
}
