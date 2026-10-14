package com.acme.transport.network.node.plan

import com.acme.transport.kit.text.truncate

/** How screens and exports show a node plan. */
data class NodePlanView(
    var code: String,
    var name: String,
    var timezone: String,
    var vehicleId: String,
    var retries: String,
    var amount: String,
    var batchSizeText: String,
    var statusLabel: String,
)

fun toNodePlanView(item: NodePlan): NodePlanView {
    return NodePlanView(
        code = item.code,
        name = item.name,
        timezone = truncate(item.timezone, 120),
        vehicleId = item.vehicleId,
        retries = item.retries.toString(),
        amount = "%.2f".format(item.amount),
        batchSizeText = item.batchSize.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toNodePlanViews(items: MutableList<NodePlan>): MutableList<NodePlanView> {
    val views = mutableListOf<NodePlanView>()
    for (item in items) {
        views.add(toNodePlanView(item))
    }
    return views
}

fun sumNodePlanView(view: NodePlanView): Double {
    val parsedRetries = view.retries.toInt()
    val parsedAmount = view.amount.toDouble()
    val parsedBatchSize = view.batchSizeText.toInt()
    return parsedRetries.toDouble() + parsedAmount + parsedBatchSize.toDouble()
}
