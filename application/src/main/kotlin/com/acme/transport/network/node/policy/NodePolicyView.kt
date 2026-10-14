package com.acme.transport.network.node.policy

import com.acme.transport.kit.text.truncate

/** How screens and exports show a node policy. */
data class NodePolicyView(
    var code: String,
    var title: String,
    var stopCount: String,
    var lane: String,
    var pallets: String,
    var summary: String,
    var budget: String,
    var localeText: String,
    var statusLabel: String,
)

fun toNodePolicyView(item: NodePolicy): NodePolicyView {
    return NodePolicyView(
        code = item.code,
        title = truncate(item.title, 120),
        stopCount = item.stopCount.toString(),
        lane = item.lane,
        pallets = item.pallets.toString(),
        summary = item.summary,
        budget = "%.2f".format(item.budget),
        localeText = item.locale,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toNodePolicyViews(items: MutableList<NodePolicy>): MutableList<NodePolicyView> {
    val views = mutableListOf<NodePolicyView>()
    for (item in items) {
        views.add(toNodePolicyView(item))
    }
    return views
}

fun sumNodePolicyView(view: NodePolicyView): Double {
    val parsedStopCount = view.stopCount.toInt()
    val parsedPallets = view.pallets.toInt()
    val parsedBudget = view.budget.toDouble()
    return parsedStopCount.toDouble() + parsedPallets.toDouble() + parsedBudget
}
