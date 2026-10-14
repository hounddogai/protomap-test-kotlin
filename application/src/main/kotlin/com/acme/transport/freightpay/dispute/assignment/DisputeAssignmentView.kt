package com.acme.transport.freightpay.dispute.assignment

import com.acme.transport.kit.text.truncate

/** How screens and exports show a dispute assignment. */
data class DisputeAssignmentView(
    var code: String,
    var label: String,
    var descriptionText: String,
    var country: String,
    var maximum: String,
    var createdAt: String,
    var driverId: String,
    var minimum: String,
    var summary: String,
    var ratio: String,
    var shiftCode: String,
    var statusLabel: String,
)

fun toDisputeAssignmentView(item: DisputeAssignment): DisputeAssignmentView {
    return DisputeAssignmentView(
        code = item.code,
        label = truncate(item.label, 64),
        descriptionText = item.description,
        country = item.country,
        maximum = "%.2f".format(item.maximum),
        createdAt = item.createdAt.toString(),
        driverId = truncate(item.driverId, 40),
        minimum = "%.2f".format(item.minimum),
        summary = truncate(item.summary, 120),
        ratio = "%.2f".format(item.ratio),
        shiftCode = item.shiftCode,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDisputeAssignmentViews(items: MutableList<DisputeAssignment>): MutableList<DisputeAssignmentView> {
    val views = mutableListOf<DisputeAssignmentView>()
    for (item in items) {
        views.add(toDisputeAssignmentView(item))
    }
    return views
}

fun sumDisputeAssignmentView(view: DisputeAssignmentView): Double {
    val parsedMaximum = view.maximum.toDouble()
    val parsedCreatedAt = view.createdAt.toInt()
    val parsedMinimum = view.minimum.toDouble()
    return parsedMaximum + parsedCreatedAt.toDouble() + parsedMinimum
}
