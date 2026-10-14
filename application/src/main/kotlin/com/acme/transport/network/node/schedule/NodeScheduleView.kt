package com.acme.transport.network.node.schedule

import com.acme.transport.kit.text.truncate

/** How screens and exports show a node schedule. */
data class NodeScheduleView(
    var code: String,
    var label: String,
    var discount: String,
    var limit: String,
    var sequence: String,
    var fuelLiters: String,
    var rateText: String,
    var etaMinutes: String,
    var expiresAtText: String,
    var odometerText: String,
    var depot: String,
    var statusLabel: String,
)

fun toNodeScheduleView(item: NodeSchedule): NodeScheduleView {
    return NodeScheduleView(
        code = item.code,
        label = item.label,
        discount = "%.2f".format(item.discount),
        limit = item.limit.toString(),
        sequence = item.sequence.toString(),
        fuelLiters = "%.2f".format(item.fuelLiters),
        rateText = "%.2f".format(item.rate),
        etaMinutes = item.etaMinutes.toString(),
        expiresAtText = item.expiresAt.toString(),
        odometerText = item.odometer.toString(),
        depot = truncate(item.depot, 64),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toNodeScheduleViews(items: MutableList<NodeSchedule>): MutableList<NodeScheduleView> {
    val views = mutableListOf<NodeScheduleView>()
    for (item in items) {
        views.add(toNodeScheduleView(item))
    }
    return views
}

fun sumNodeScheduleView(view: NodeScheduleView): Double {
    val parsedDiscount = view.discount.toDouble()
    val parsedLimit = view.limit.toInt()
    val parsedSequence = view.sequence.toInt()
    return parsedDiscount + parsedLimit.toDouble() + parsedSequence.toDouble()
}
