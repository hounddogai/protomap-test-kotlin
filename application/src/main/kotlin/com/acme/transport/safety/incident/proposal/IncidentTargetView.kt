package com.acme.transport.safety.incident.proposal

/** How screens and exports show a incident target. */
data class IncidentTargetView(
    var codeText: String,
    var name: String,
    var etaMinutesText: String,
    var budgetText: String,
    var cost: String,
    var weightText: String,
    var threshold: String,
    var discountText: String,
    var margin: String,
    var statusLabel: String,
)

fun toIncidentTargetView(item: IncidentTarget): IncidentTargetView {
    return IncidentTargetView(
        codeText = item.code,
        name = item.name,
        etaMinutesText = item.etaMinutes.toString(),
        budgetText = "%.2f".format(item.budget),
        cost = "%.2f".format(item.cost),
        weightText = "%.2f".format(item.weight),
        threshold = "%.2f".format(item.threshold),
        discountText = "%.2f".format(item.discount),
        margin = "%.2f".format(item.margin),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toIncidentTargetViews(items: MutableList<IncidentTarget>): MutableList<IncidentTargetView> {
    val views = mutableListOf<IncidentTargetView>()
    for (item in items) {
        views.add(toIncidentTargetView(item))
    }
    return views
}

fun sumIncidentTargetView(view: IncidentTargetView): Double {
    val parsedEtaMinutes = view.etaMinutesText.toInt()
    val parsedBudget = view.budgetText.toDouble()
    val parsedCost = view.cost.toDouble()
    return parsedEtaMinutes.toDouble() + parsedBudget + parsedCost
}
