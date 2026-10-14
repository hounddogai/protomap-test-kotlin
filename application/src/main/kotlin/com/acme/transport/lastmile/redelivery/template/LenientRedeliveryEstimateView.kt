package com.acme.transport.lastmile.redelivery.template

import com.acme.transport.kit.text.truncate

/** How screens and exports show a lenient redelivery estimate. */
data class LenientRedeliveryEstimateView(
    var code: String,
    var title: String,
    var capacity: String,
    var budgetText: String,
    var depot: String,
    var routeCode: String,
    var weight: String,
    var statusLabel: String,
)

fun toLenientRedeliveryEstimateView(item: LenientRedeliveryEstimate): LenientRedeliveryEstimateView {
    return LenientRedeliveryEstimateView(
        code = item.code,
        title = truncate(item.title, 40),
        capacity = item.capacity.toString(),
        budgetText = "%.2f".format(item.budget),
        depot = truncate(item.depot, 64),
        routeCode = item.routeCode,
        weight = "%.2f".format(item.weight),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLenientRedeliveryEstimateViews(items: MutableList<LenientRedeliveryEstimate>): MutableList<LenientRedeliveryEstimateView> {
    val views = mutableListOf<LenientRedeliveryEstimateView>()
    for (item in items) {
        views.add(toLenientRedeliveryEstimateView(item))
    }
    return views
}

fun sumLenientRedeliveryEstimateView(view: LenientRedeliveryEstimateView): Double {
    val parsedCapacity = view.capacity.toInt()
    val parsedBudget = view.budgetText.toDouble()
    val parsedWeight = view.weight.toDouble()
    return parsedCapacity.toDouble() + parsedBudget + parsedWeight
}
