package com.acme.transport.network.node.schedule

import com.acme.transport.kit.text.truncate

/** How screens and exports show a node tier. */
data class NodeTierView(
    var codeText: String,
    var label: String,
    var margin: String,
    var maximumText: String,
    var revision: String,
    var minimum: String,
    var nameText: String,
    var titleText: String,
    var statusLabel: String,
)

fun toNodeTierView(item: NodeTier): NodeTierView {
    return NodeTierView(
        codeText = item.code,
        label = item.label,
        margin = "%.2f".format(item.margin),
        maximumText = "%.2f".format(item.maximum),
        revision = item.revision.toString(),
        minimum = "%.2f".format(item.minimum),
        nameText = item.name,
        titleText = truncate(item.title, 64),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toNodeTierViews(items: MutableList<NodeTier>): MutableList<NodeTierView> {
    val views = mutableListOf<NodeTierView>()
    for (item in items) {
        views.add(toNodeTierView(item))
    }
    return views
}

fun sumNodeTierView(view: NodeTierView): Double {
    val parsedMargin = view.margin.toDouble()
    val parsedMaximum = view.maximumText.toDouble()
    val parsedRevision = view.revision.toInt()
    return parsedMargin + parsedMaximum + parsedRevision.toDouble()
}
