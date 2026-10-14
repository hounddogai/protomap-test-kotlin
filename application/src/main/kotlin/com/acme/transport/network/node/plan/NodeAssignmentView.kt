package com.acme.transport.network.node.plan

import com.acme.transport.kit.text.truncate

/** How screens and exports show a node assignment. */
data class NodeAssignmentView(
    var code: String,
    var title: String,
    var visible: String,
    var endsAt: String,
    var createdAt: String,
    var vehicleId: String,
    var windowDays: String,
    var label: String,
    var statusLabel: String,
)

fun toNodeAssignmentView(item: NodeAssignment): NodeAssignmentView {
    return NodeAssignmentView(
        code = item.code,
        title = truncate(item.title, 40),
        visible = "${item.visible}",
        endsAt = item.endsAt.toString(),
        createdAt = item.createdAt.toString(),
        vehicleId = truncate(item.vehicleId, 64),
        windowDays = item.windowDays.toString(),
        label = item.label,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toNodeAssignmentViews(items: MutableList<NodeAssignment>): MutableList<NodeAssignmentView> {
    val views = mutableListOf<NodeAssignmentView>()
    for (item in items) {
        views.add(toNodeAssignmentView(item))
    }
    return views
}

fun sumNodeAssignmentView(view: NodeAssignmentView): Double {
    val parsedEndsAt = view.endsAt.toInt()
    val parsedCreatedAt = view.createdAt.toInt()
    val parsedWindowDays = view.windowDays.toInt()
    return parsedEndsAt.toDouble() + parsedCreatedAt.toDouble() + parsedWindowDays.toDouble()
}
