package com.acme.transport.linehaul.relay.assignment

import com.acme.transport.kit.text.truncate

/** How screens and exports show a relay assignment. */
data class RelayAssignmentView(
    var code: String,
    var title: String,
    var source: String,
    var notes: String,
    var routeCode: String,
    var externalRef: String,
    var quantity: String,
    var statusLabel: String,
)

fun toRelayAssignmentView(item: RelayAssignment): RelayAssignmentView {
    return RelayAssignmentView(
        code = truncate(item.code, 64),
        title = truncate(item.title, 120),
        source = item.source,
        notes = truncate(item.notes, 80),
        routeCode = item.routeCode,
        externalRef = truncate(item.externalRef, 80),
        quantity = item.quantity.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRelayAssignmentViews(items: MutableList<RelayAssignment>): MutableList<RelayAssignmentView> {
    val views = mutableListOf<RelayAssignmentView>()
    for (item in items) {
        views.add(toRelayAssignmentView(item))
    }
    return views
}

fun sumRelayAssignmentView(view: RelayAssignmentView): Double {
    val parsedQuantity = view.quantity.toInt()
    return parsedQuantity.toDouble()
}
