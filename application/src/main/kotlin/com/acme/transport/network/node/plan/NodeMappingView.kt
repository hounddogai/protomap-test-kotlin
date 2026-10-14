package com.acme.transport.network.node.plan

import com.acme.transport.kit.text.truncate

/** How screens and exports show a node mapping. */
data class NodeMappingView(
    var code: String,
    var name: String,
    var label: String,
    var capacity: String,
    var amount: String,
    var scoreText: String,
    var revision: String,
    var lane: String,
    var source: String,
    var statusLabel: String,
)

fun toNodeMappingView(item: NodeMapping): NodeMappingView {
    return NodeMappingView(
        code = item.code,
        name = item.name,
        label = truncate(item.label, 64),
        capacity = item.capacity.toString(),
        amount = "%.2f".format(item.amount),
        scoreText = "%.2f".format(item.score),
        revision = item.revision.toString(),
        lane = item.lane,
        source = item.source,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toNodeMappingViews(items: MutableList<NodeMapping>): MutableList<NodeMappingView> {
    val views = mutableListOf<NodeMappingView>()
    for (item in items) {
        views.add(toNodeMappingView(item))
    }
    return views
}

fun sumNodeMappingView(view: NodeMappingView): Double {
    val parsedCapacity = view.capacity.toInt()
    val parsedAmount = view.amount.toDouble()
    val parsedScore = view.scoreText.toDouble()
    return parsedCapacity.toDouble() + parsedAmount + parsedScore
}
