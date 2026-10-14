package com.acme.transport.network.node.draft

import com.acme.transport.kit.text.truncate

/** How screens and exports show a node reservation. */
data class NodeReservationView(
    var code: String,
    var titleText: String,
    var carrier: String,
    var weightText: String,
    var label: String,
    var visibleText: String,
    var statusLabel: String,
)

fun toNodeReservationView(item: NodeReservation): NodeReservationView {
    return NodeReservationView(
        code = item.code,
        titleText = item.title,
        carrier = truncate(item.carrier, 64),
        weightText = "%.2f".format(item.weight),
        label = item.label,
        visibleText = "${item.visible}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toNodeReservationViews(items: MutableList<NodeReservation>): MutableList<NodeReservationView> {
    val views = mutableListOf<NodeReservationView>()
    for (item in items) {
        views.add(toNodeReservationView(item))
    }
    return views
}

fun sumNodeReservationView(view: NodeReservationView): Double {
    val parsedWeight = view.weightText.toDouble()
    return parsedWeight
}
