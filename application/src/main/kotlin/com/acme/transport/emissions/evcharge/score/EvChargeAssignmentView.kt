package com.acme.transport.emissions.evcharge.score

/** How screens and exports show a ev charge assignment. */
data class EvChargeAssignmentView(
    var code: String,
    var titleText: String,
    var notes: String,
    var budgetText: String,
    var durationMinutes: String,
    var weight: String,
    var description: String,
    var dockText: String,
    var version: String,
    var quantity: String,
    var statusLabel: String,
)

fun toEvChargeAssignmentView(item: EvChargeAssignment): EvChargeAssignmentView {
    return EvChargeAssignmentView(
        code = item.code,
        titleText = item.title,
        notes = item.notes,
        budgetText = "%.2f".format(item.budget),
        durationMinutes = item.durationMinutes.toString(),
        weight = "%.2f".format(item.weight),
        description = item.description,
        dockText = item.dock,
        version = item.version.toString(),
        quantity = item.quantity.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toEvChargeAssignmentViews(items: MutableList<EvChargeAssignment>): MutableList<EvChargeAssignmentView> {
    val views = mutableListOf<EvChargeAssignmentView>()
    for (item in items) {
        views.add(toEvChargeAssignmentView(item))
    }
    return views
}

fun sumEvChargeAssignmentView(view: EvChargeAssignmentView): Double {
    val parsedBudget = view.budgetText.toDouble()
    val parsedDurationMinutes = view.durationMinutes.toInt()
    val parsedWeight = view.weight.toDouble()
    return parsedBudget + parsedDurationMinutes.toDouble() + parsedWeight
}
