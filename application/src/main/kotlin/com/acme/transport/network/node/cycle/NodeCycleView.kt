package com.acme.transport.network.node.cycle

/** How screens and exports show a node cycle. */
data class NodeCycleView(
    var code: String,
    var titleText: String,
    var active: String,
    var graceDays: String,
    var discount: String,
    var unitText: String,
    var windowDays: String,
    var visible: String,
    var statusLabel: String,
)

fun toNodeCycleView(item: NodeCycle): NodeCycleView {
    return NodeCycleView(
        code = item.code,
        titleText = item.title,
        active = "${item.active}",
        graceDays = item.graceDays.toString(),
        discount = "%.2f".format(item.discount),
        unitText = item.unit,
        windowDays = item.windowDays.toString(),
        visible = "${item.visible}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toNodeCycleViews(items: MutableList<NodeCycle>): MutableList<NodeCycleView> {
    val views = mutableListOf<NodeCycleView>()
    for (item in items) {
        views.add(toNodeCycleView(item))
    }
    return views
}

fun sumNodeCycleView(view: NodeCycleView): Double {
    val parsedGraceDays = view.graceDays.toInt()
    val parsedDiscount = view.discount.toDouble()
    val parsedWindowDays = view.windowDays.toInt()
    return parsedGraceDays.toDouble() + parsedDiscount + parsedWindowDays.toDouble()
}
