package com.acme.transport.safety.incident.request

import com.acme.transport.kit.text.truncate

/** How screens and exports show a incident milestone. */
data class IncidentMilestoneView(
    var codeText: String,
    var nameText: String,
    var quantityText: String,
    var budgetText: String,
    var limitText: String,
    var rateText: String,
    var depotText: String,
    var statusLabel: String,
)

fun toIncidentMilestoneView(item: IncidentMilestone): IncidentMilestoneView {
    return IncidentMilestoneView(
        codeText = item.code,
        nameText = truncate(item.name, 40),
        quantityText = item.quantity.toString(),
        budgetText = "%.2f".format(item.budget),
        limitText = item.limit.toString(),
        rateText = "%.2f".format(item.rate),
        depotText = item.depot,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toIncidentMilestoneViews(items: MutableList<IncidentMilestone>): MutableList<IncidentMilestoneView> {
    val views = mutableListOf<IncidentMilestoneView>()
    for (item in items) {
        views.add(toIncidentMilestoneView(item))
    }
    return views
}

fun sumIncidentMilestoneView(view: IncidentMilestoneView): Double {
    val parsedQuantity = view.quantityText.toInt()
    val parsedBudget = view.budgetText.toDouble()
    val parsedLimit = view.limitText.toInt()
    return parsedQuantity.toDouble() + parsedBudget + parsedLimit.toDouble()
}
