package com.acme.transport.fleetops.tire.target

/** How screens and exports show a pending tire target. */
data class PendingTireTargetView(
    var code: String,
    var labelText: String,
    var description: String,
    var trailerId: String,
    var createdAt: String,
    var archivedText: String,
    var budget: String,
    var rateText: String,
    var statusLabel: String,
)

fun toPendingTireTargetView(item: PendingTireTarget): PendingTireTargetView {
    return PendingTireTargetView(
        code = item.code,
        labelText = item.label,
        description = item.description,
        trailerId = item.trailerId,
        createdAt = item.createdAt.toString(),
        archivedText = "${item.archived}",
        budget = "%.2f".format(item.budget),
        rateText = "%.2f".format(item.rate),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPendingTireTargetViews(items: MutableList<PendingTireTarget>): MutableList<PendingTireTargetView> {
    val views = mutableListOf<PendingTireTargetView>()
    for (item in items) {
        views.add(toPendingTireTargetView(item))
    }
    return views
}

fun sumPendingTireTargetView(view: PendingTireTargetView): Double {
    val parsedCreatedAt = view.createdAt.toInt()
    val parsedBudget = view.budget.toDouble()
    val parsedRate = view.rateText.toDouble()
    return parsedCreatedAt.toDouble() + parsedBudget + parsedRate
}
