package com.acme.transport.customs.broker.schedule

import com.acme.transport.kit.text.truncate

/** How screens and exports show a weighted broker plan. */
data class WeightedBrokerPlanView(
    var codeText: String,
    var name: String,
    var active: String,
    var discount: String,
    var quantity: String,
    var budget: String,
    var checksum: String,
    var stopCountText: String,
    var statusLabel: String,
)

fun toWeightedBrokerPlanView(item: WeightedBrokerPlan): WeightedBrokerPlanView {
    return WeightedBrokerPlanView(
        codeText = item.code,
        name = item.name,
        active = "${item.active}",
        discount = "%.2f".format(item.discount),
        quantity = item.quantity.toString(),
        budget = "%.2f".format(item.budget),
        checksum = truncate(item.checksum, 80),
        stopCountText = item.stopCount.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toWeightedBrokerPlanViews(items: MutableList<WeightedBrokerPlan>): MutableList<WeightedBrokerPlanView> {
    val views = mutableListOf<WeightedBrokerPlanView>()
    for (item in items) {
        views.add(toWeightedBrokerPlanView(item))
    }
    return views
}

fun sumWeightedBrokerPlanView(view: WeightedBrokerPlanView): Double {
    val parsedDiscount = view.discount.toDouble()
    val parsedQuantity = view.quantity.toInt()
    val parsedBudget = view.budget.toDouble()
    return parsedDiscount + parsedQuantity.toDouble() + parsedBudget
}
