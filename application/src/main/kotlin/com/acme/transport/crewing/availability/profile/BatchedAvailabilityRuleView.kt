package com.acme.transport.crewing.availability.profile

import com.acme.transport.kit.text.truncate

/** How screens and exports show a batched availability rule. */
data class BatchedAvailabilityRuleView(
    var codeText: String,
    var name: String,
    var summary: String,
    var maximum: String,
    var routeCodeText: String,
    var stopCount: String,
    var quantity: String,
    var costText: String,
    var phoneText: String,
    var labelText: String,
    var locale: String,
    var statusLabel: String,
)

fun toBatchedAvailabilityRuleView(item: BatchedAvailabilityRule): BatchedAvailabilityRuleView {
    return BatchedAvailabilityRuleView(
        codeText = item.code,
        name = item.name,
        summary = item.summary,
        maximum = "%.2f".format(item.maximum),
        routeCodeText = item.routeCode,
        stopCount = item.stopCount.toString(),
        quantity = item.quantity.toString(),
        costText = "%.2f".format(item.cost),
        phoneText = truncate(item.phone, 80),
        labelText = truncate(item.label, 64),
        locale = item.locale,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toBatchedAvailabilityRuleViews(items: MutableList<BatchedAvailabilityRule>): MutableList<BatchedAvailabilityRuleView> {
    val views = mutableListOf<BatchedAvailabilityRuleView>()
    for (item in items) {
        views.add(toBatchedAvailabilityRuleView(item))
    }
    return views
}

fun sumBatchedAvailabilityRuleView(view: BatchedAvailabilityRuleView): Double {
    val parsedMaximum = view.maximum.toDouble()
    val parsedStopCount = view.stopCount.toInt()
    val parsedQuantity = view.quantity.toInt()
    return parsedMaximum + parsedStopCount.toDouble() + parsedQuantity.toDouble()
}
