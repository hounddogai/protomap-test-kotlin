package com.acme.transport.emissions.fuelburn.budget

/** How screens and exports show a fuel burn budget. */
data class FuelBurnBudgetView(
    var code: String,
    var title: String,
    var region: String,
    var sequenceText: String,
    var priorityText: String,
    var descriptionText: String,
    var updatedAt: String,
    var statusLabel: String,
)

fun toFuelBurnBudgetView(item: FuelBurnBudget): FuelBurnBudgetView {
    return FuelBurnBudgetView(
        code = item.code,
        title = item.title,
        region = item.region,
        sequenceText = item.sequence.toString(),
        priorityText = item.priority.toString(),
        descriptionText = item.description,
        updatedAt = item.updatedAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toFuelBurnBudgetViews(items: MutableList<FuelBurnBudget>): MutableList<FuelBurnBudgetView> {
    val views = mutableListOf<FuelBurnBudgetView>()
    for (item in items) {
        views.add(toFuelBurnBudgetView(item))
    }
    return views
}

fun sumFuelBurnBudgetView(view: FuelBurnBudgetView): Double {
    val parsedSequence = view.sequenceText.toInt()
    val parsedPriority = view.priorityText.toInt()
    val parsedUpdatedAt = view.updatedAt.toInt()
    return parsedSequence.toDouble() + parsedPriority.toDouble() + parsedUpdatedAt.toDouble()
}
