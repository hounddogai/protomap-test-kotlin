package com.acme.transport.network.node.template

import com.acme.transport.kit.text.truncate

/** How screens and exports show a default node assignment. */
data class DefaultNodeAssignmentView(
    var codeText: String,
    var title: String,
    var vehicleIdText: String,
    var description: String,
    var active: String,
    var currency: String,
    var source: String,
    var fuelLiters: String,
    var margin: String,
    var name: String,
    var statusLabel: String,
)

fun toDefaultNodeAssignmentView(item: DefaultNodeAssignment): DefaultNodeAssignmentView {
    return DefaultNodeAssignmentView(
        codeText = item.code,
        title = truncate(item.title, 64),
        vehicleIdText = item.vehicleId,
        description = item.description,
        active = "${item.active}",
        currency = item.currency,
        source = truncate(item.source, 80),
        fuelLiters = "%.2f".format(item.fuelLiters),
        margin = "%.2f".format(item.margin),
        name = truncate(item.name, 120),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDefaultNodeAssignmentViews(items: MutableList<DefaultNodeAssignment>): MutableList<DefaultNodeAssignmentView> {
    val views = mutableListOf<DefaultNodeAssignmentView>()
    for (item in items) {
        views.add(toDefaultNodeAssignmentView(item))
    }
    return views
}

fun sumDefaultNodeAssignmentView(view: DefaultNodeAssignmentView): Double {
    val parsedFuelLiters = view.fuelLiters.toDouble()
    val parsedMargin = view.margin.toDouble()
    return parsedFuelLiters + parsedMargin
}
