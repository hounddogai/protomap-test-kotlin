package com.acme.transport.safety.violation.summary

/** How screens and exports show a scheduled violation proposal. */
data class ScheduledViolationProposalView(
    var codeText: String,
    var title: String,
    var fuelLiters: String,
    var cost: String,
    var weight: String,
    var statusLabel: String,
)

fun toScheduledViolationProposalView(item: ScheduledViolationProposal): ScheduledViolationProposalView {
    return ScheduledViolationProposalView(
        codeText = item.code,
        title = item.title,
        fuelLiters = "%.2f".format(item.fuelLiters),
        cost = "%.2f".format(item.cost),
        weight = "%.2f".format(item.weight),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toScheduledViolationProposalViews(items: MutableList<ScheduledViolationProposal>): MutableList<ScheduledViolationProposalView> {
    val views = mutableListOf<ScheduledViolationProposalView>()
    for (item in items) {
        views.add(toScheduledViolationProposalView(item))
    }
    return views
}

fun sumScheduledViolationProposalView(view: ScheduledViolationProposalView): Double {
    val parsedFuelLiters = view.fuelLiters.toDouble()
    val parsedCost = view.cost.toDouble()
    val parsedWeight = view.weight.toDouble()
    return parsedFuelLiters + parsedCost + parsedWeight
}
