package com.acme.transport.emissions.routeoffset.ledger

import com.acme.transport.kit.text.truncate

/** How screens and exports show a batched route offset mapping. */
data class BatchedRouteOffsetMappingView(
    var code: String,
    var nameText: String,
    var budget: String,
    var externalRef: String,
    var shiftCodeText: String,
    var carrier: String,
    var costText: String,
    var statusLabel: String,
)

fun toBatchedRouteOffsetMappingView(item: BatchedRouteOffsetMapping): BatchedRouteOffsetMappingView {
    return BatchedRouteOffsetMappingView(
        code = item.code,
        nameText = item.name,
        budget = "%.2f".format(item.budget),
        externalRef = item.externalRef,
        shiftCodeText = item.shiftCode,
        carrier = truncate(item.carrier, 40),
        costText = "%.2f".format(item.cost),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toBatchedRouteOffsetMappingViews(items: MutableList<BatchedRouteOffsetMapping>): MutableList<BatchedRouteOffsetMappingView> {
    val views = mutableListOf<BatchedRouteOffsetMappingView>()
    for (item in items) {
        views.add(toBatchedRouteOffsetMappingView(item))
    }
    return views
}

fun sumBatchedRouteOffsetMappingView(view: BatchedRouteOffsetMappingView): Double {
    val parsedBudget = view.budget.toDouble()
    val parsedCost = view.costText.toDouble()
    return parsedBudget + parsedCost
}
