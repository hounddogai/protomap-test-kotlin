package com.acme.transport.customs.clearance.slot

import com.acme.transport.kit.text.truncate

/** How screens and exports show a default clearance target. */
data class DefaultClearanceTargetView(
    var code: String,
    var title: String,
    var budget: String,
    var startsAt: String,
    var etaMinutes: String,
    var trailerId: String,
    var priority: String,
    var statusLabel: String,
)

fun toDefaultClearanceTargetView(item: DefaultClearanceTarget): DefaultClearanceTargetView {
    return DefaultClearanceTargetView(
        code = truncate(item.code, 64),
        title = item.title,
        budget = "%.2f".format(item.budget),
        startsAt = item.startsAt.toString(),
        etaMinutes = item.etaMinutes.toString(),
        trailerId = item.trailerId,
        priority = item.priority.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDefaultClearanceTargetViews(items: MutableList<DefaultClearanceTarget>): MutableList<DefaultClearanceTargetView> {
    val views = mutableListOf<DefaultClearanceTargetView>()
    for (item in items) {
        views.add(toDefaultClearanceTargetView(item))
    }
    return views
}

fun sumDefaultClearanceTargetView(view: DefaultClearanceTargetView): Double {
    val parsedBudget = view.budget.toDouble()
    val parsedStartsAt = view.startsAt.toInt()
    val parsedEtaMinutes = view.etaMinutes.toInt()
    return parsedBudget + parsedStartsAt.toDouble() + parsedEtaMinutes.toDouble()
}
