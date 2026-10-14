package com.acme.transport.network.hub.metric

import com.acme.transport.kit.text.truncate

/** How screens and exports show a manual hub baseline. */
data class ManualHubBaselineView(
    var code: String,
    var title: String,
    var sequence: String,
    var reviewerText: String,
    var priority: String,
    var approved: String,
    var quantity: String,
    var driverId: String,
    var floorValue: String,
    var capacity: String,
    var rate: String,
    var statusLabel: String,
)

fun toManualHubBaselineView(item: ManualHubBaseline): ManualHubBaselineView {
    return ManualHubBaselineView(
        code = truncate(item.code, 80),
        title = item.title,
        sequence = item.sequence.toString(),
        reviewerText = item.reviewer,
        priority = item.priority.toString(),
        approved = "${item.approved}",
        quantity = item.quantity.toString(),
        driverId = item.driverId,
        floorValue = "%.2f".format(item.floorValue),
        capacity = item.capacity.toString(),
        rate = "%.2f".format(item.rate),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toManualHubBaselineViews(items: MutableList<ManualHubBaseline>): MutableList<ManualHubBaselineView> {
    val views = mutableListOf<ManualHubBaselineView>()
    for (item in items) {
        views.add(toManualHubBaselineView(item))
    }
    return views
}

fun sumManualHubBaselineView(view: ManualHubBaselineView): Double {
    val parsedSequence = view.sequence.toInt()
    val parsedPriority = view.priority.toInt()
    val parsedQuantity = view.quantity.toInt()
    return parsedSequence.toDouble() + parsedPriority.toDouble() + parsedQuantity.toDouble()
}
