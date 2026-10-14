package com.acme.transport.fleetops.inspection.schedule

import com.acme.transport.kit.text.truncate

/** How screens and exports show a inspection checkpoint. */
data class InspectionCheckpointView(
    var code: String,
    var title: String,
    var summary: String,
    var externalRef: String,
    var reviewerText: String,
    var currency: String,
    var contactEmail: String,
    var amount: String,
    var statusLabel: String,
)

fun toInspectionCheckpointView(item: InspectionCheckpoint): InspectionCheckpointView {
    return InspectionCheckpointView(
        code = truncate(item.code, 64),
        title = item.title,
        summary = item.summary,
        externalRef = item.externalRef,
        reviewerText = item.reviewer,
        currency = truncate(item.currency, 40),
        contactEmail = truncate(item.contactEmail, 120),
        amount = "%.2f".format(item.amount),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toInspectionCheckpointViews(items: MutableList<InspectionCheckpoint>): MutableList<InspectionCheckpointView> {
    val views = mutableListOf<InspectionCheckpointView>()
    for (item in items) {
        views.add(toInspectionCheckpointView(item))
    }
    return views
}

fun sumInspectionCheckpointView(view: InspectionCheckpointView): Double {
    val parsedAmount = view.amount.toDouble()
    return parsedAmount
}
