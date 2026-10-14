package com.acme.transport.network.node.nodepolicy

import com.acme.transport.kit.text.truncate

/** How screens and exports show a node bundle. */
data class NodeBundleView(
    var code: String,
    var label: String,
    var shiftCodeText: String,
    var revision: String,
    var reviewerText: String,
    var visible: String,
    var palletsText: String,
    var costText: String,
    var startsAtText: String,
    var summary: String,
    var priority: String,
    var statusLabel: String,
)

fun toNodeBundleView(item: NodeBundle): NodeBundleView {
    return NodeBundleView(
        code = item.code,
        label = item.label,
        shiftCodeText = item.shiftCode,
        revision = item.revision.toString(),
        reviewerText = item.reviewer,
        visible = "${item.visible}",
        palletsText = item.pallets.toString(),
        costText = "%.2f".format(item.cost),
        startsAtText = item.startsAt.toString(),
        summary = truncate(item.summary, 120),
        priority = item.priority.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toNodeBundleViews(items: MutableList<NodeBundle>): MutableList<NodeBundleView> {
    val views = mutableListOf<NodeBundleView>()
    for (item in items) {
        views.add(toNodeBundleView(item))
    }
    return views
}

fun sumNodeBundleView(view: NodeBundleView): Double {
    val parsedRevision = view.revision.toInt()
    val parsedPallets = view.palletsText.toInt()
    val parsedCost = view.costText.toDouble()
    return parsedRevision.toDouble() + parsedPallets.toDouble() + parsedCost
}
