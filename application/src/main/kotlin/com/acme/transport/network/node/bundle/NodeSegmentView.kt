package com.acme.transport.network.node.bundle

/** How screens and exports show a node segment. */
data class NodeSegmentView(
    var code: String,
    var label: String,
    var priority: String,
    var createdAt: String,
    var budget: String,
    var revision: String,
    var retriesText: String,
    var ratioText: String,
    var category: String,
    var statusLabel: String,
)

fun toNodeSegmentView(item: NodeSegment): NodeSegmentView {
    return NodeSegmentView(
        code = item.code,
        label = item.label,
        priority = item.priority.toString(),
        createdAt = item.createdAt.toString(),
        budget = "%.2f".format(item.budget),
        revision = item.revision.toString(),
        retriesText = item.retries.toString(),
        ratioText = "%.2f".format(item.ratio),
        category = item.category,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toNodeSegmentViews(items: MutableList<NodeSegment>): MutableList<NodeSegmentView> {
    val views = mutableListOf<NodeSegmentView>()
    for (item in items) {
        views.add(toNodeSegmentView(item))
    }
    return views
}

fun sumNodeSegmentView(view: NodeSegmentView): Double {
    val parsedPriority = view.priority.toInt()
    val parsedCreatedAt = view.createdAt.toInt()
    val parsedBudget = view.budget.toDouble()
    return parsedPriority.toDouble() + parsedCreatedAt.toDouble() + parsedBudget
}
