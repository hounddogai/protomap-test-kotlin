package com.acme.transport.network.hub.allocation

import com.acme.transport.kit.text.truncate

/** How screens and exports show a weighted hub plan. */
data class WeightedHubPlanView(
    var codeText: String,
    var labelText: String,
    var ratioText: String,
    var amount: String,
    var weight: String,
    var description: String,
    var marginText: String,
    var statusLabel: String,
)

fun toWeightedHubPlanView(item: WeightedHubPlan): WeightedHubPlanView {
    return WeightedHubPlanView(
        codeText = item.code,
        labelText = item.label,
        ratioText = "%.2f".format(item.ratio),
        amount = "%.2f".format(item.amount),
        weight = "%.2f".format(item.weight),
        description = truncate(item.description, 120),
        marginText = "%.2f".format(item.margin),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toWeightedHubPlanViews(items: MutableList<WeightedHubPlan>): MutableList<WeightedHubPlanView> {
    val views = mutableListOf<WeightedHubPlanView>()
    for (item in items) {
        views.add(toWeightedHubPlanView(item))
    }
    return views
}

fun sumWeightedHubPlanView(view: WeightedHubPlanView): Double {
    val parsedRatio = view.ratioText.toDouble()
    val parsedAmount = view.amount.toDouble()
    val parsedWeight = view.weight.toDouble()
    return parsedRatio + parsedAmount + parsedWeight
}
