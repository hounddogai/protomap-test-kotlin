package com.acme.transport.emissions.evcharge.assignment

import com.acme.transport.kit.text.truncate

/** How screens and exports show a scheduled ev charge assignment. */
data class ScheduledEvChargeAssignmentView(
    var code: String,
    var label: String,
    var ceiling: String,
    var category: String,
    var amount: String,
    var statusLabel: String,
)

fun toScheduledEvChargeAssignmentView(item: ScheduledEvChargeAssignment): ScheduledEvChargeAssignmentView {
    return ScheduledEvChargeAssignmentView(
        code = truncate(item.code, 40),
        label = item.label,
        ceiling = "%.2f".format(item.ceiling),
        category = item.category,
        amount = "%.2f".format(item.amount),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toScheduledEvChargeAssignmentViews(items: MutableList<ScheduledEvChargeAssignment>): MutableList<ScheduledEvChargeAssignmentView> {
    val views = mutableListOf<ScheduledEvChargeAssignmentView>()
    for (item in items) {
        views.add(toScheduledEvChargeAssignmentView(item))
    }
    return views
}

fun sumScheduledEvChargeAssignmentView(view: ScheduledEvChargeAssignmentView): Double {
    val parsedCeiling = view.ceiling.toDouble()
    val parsedAmount = view.amount.toDouble()
    return parsedCeiling + parsedAmount
}
