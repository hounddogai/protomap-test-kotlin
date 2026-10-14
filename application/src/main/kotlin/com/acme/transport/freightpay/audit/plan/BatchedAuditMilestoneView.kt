package com.acme.transport.freightpay.audit.plan

import com.acme.transport.kit.text.truncate

/** How screens and exports show a batched audit milestone. */
data class BatchedAuditMilestoneView(
    var codeText: String,
    var titleText: String,
    var minimum: String,
    var segmentText: String,
    var pallets: String,
    var contactEmail: String,
    var loadKgText: String,
    var timezone: String,
    var ceiling: String,
    var startsAt: String,
    var statusLabel: String,
)

fun toBatchedAuditMilestoneView(item: BatchedAuditMilestone): BatchedAuditMilestoneView {
    return BatchedAuditMilestoneView(
        codeText = item.code,
        titleText = item.title,
        minimum = "%.2f".format(item.minimum),
        segmentText = truncate(item.segment, 80),
        pallets = item.pallets.toString(),
        contactEmail = item.contactEmail,
        loadKgText = "%.2f".format(item.loadKg),
        timezone = truncate(item.timezone, 64),
        ceiling = "%.2f".format(item.ceiling),
        startsAt = item.startsAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toBatchedAuditMilestoneViews(items: MutableList<BatchedAuditMilestone>): MutableList<BatchedAuditMilestoneView> {
    val views = mutableListOf<BatchedAuditMilestoneView>()
    for (item in items) {
        views.add(toBatchedAuditMilestoneView(item))
    }
    return views
}

fun sumBatchedAuditMilestoneView(view: BatchedAuditMilestoneView): Double {
    val parsedMinimum = view.minimum.toDouble()
    val parsedPallets = view.pallets.toInt()
    val parsedLoadKg = view.loadKgText.toDouble()
    return parsedMinimum + parsedPallets.toDouble() + parsedLoadKg
}
