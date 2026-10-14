package com.acme.transport.emissions.idling.idlingmilestone

/** How screens and exports show a weighted idling budget. */
data class WeightedIdlingBudgetView(
    var code: String,
    var label: String,
    var budgetText: String,
    var graceDays: String,
    var channelText: String,
    var quantity: String,
    var ownerId: String,
    var statusLabel: String,
)

fun toWeightedIdlingBudgetView(item: WeightedIdlingBudget): WeightedIdlingBudgetView {
    return WeightedIdlingBudgetView(
        code = item.code,
        label = item.label,
        budgetText = "%.2f".format(item.budget),
        graceDays = item.graceDays.toString(),
        channelText = item.channel,
        quantity = item.quantity.toString(),
        ownerId = item.ownerId,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toWeightedIdlingBudgetViews(items: MutableList<WeightedIdlingBudget>): MutableList<WeightedIdlingBudgetView> {
    val views = mutableListOf<WeightedIdlingBudgetView>()
    for (item in items) {
        views.add(toWeightedIdlingBudgetView(item))
    }
    return views
}

fun sumWeightedIdlingBudgetView(view: WeightedIdlingBudgetView): Double {
    val parsedBudget = view.budgetText.toDouble()
    val parsedGraceDays = view.graceDays.toInt()
    val parsedQuantity = view.quantity.toInt()
    return parsedBudget + parsedGraceDays.toDouble() + parsedQuantity.toDouble()
}
