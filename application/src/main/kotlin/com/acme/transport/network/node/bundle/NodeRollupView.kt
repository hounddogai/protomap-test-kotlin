package com.acme.transport.network.node.bundle

import com.acme.transport.kit.text.truncate

/** How screens and exports show a node rollup. */
data class NodeRollupView(
    var code: String,
    var label: String,
    var priority: String,
    var dock: String,
    var marginText: String,
    var statusLabel: String,
)

fun toNodeRollupView(item: NodeRollup): NodeRollupView {
    return NodeRollupView(
        code = truncate(item.code, 120),
        label = item.label,
        priority = item.priority.toString(),
        dock = item.dock,
        marginText = "%.2f".format(item.margin),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toNodeRollupViews(items: MutableList<NodeRollup>): MutableList<NodeRollupView> {
    val views = mutableListOf<NodeRollupView>()
    for (item in items) {
        views.add(toNodeRollupView(item))
    }
    return views
}

fun sumNodeRollupView(view: NodeRollupView): Double {
    val parsedPriority = view.priority.toInt()
    val parsedMargin = view.marginText.toDouble()
    return parsedPriority.toDouble() + parsedMargin
}
