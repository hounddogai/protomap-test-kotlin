package com.acme.transport.fleetops.leasereturn.draft

import com.acme.transport.kit.text.truncate

/** How screens and exports show a batched lease return policy. */
data class BatchedLeaseReturnPolicyView(
    var code: String,
    var label: String,
    var trailerId: String,
    var quantity: String,
    var priorityText: String,
    var revisionText: String,
    var locale: String,
    var archived: String,
    var activeText: String,
    var category: String,
    var contactEmailText: String,
    var statusLabel: String,
)

fun toBatchedLeaseReturnPolicyView(item: BatchedLeaseReturnPolicy): BatchedLeaseReturnPolicyView {
    return BatchedLeaseReturnPolicyView(
        code = truncate(item.code, 80),
        label = truncate(item.label, 80),
        trailerId = item.trailerId,
        quantity = item.quantity.toString(),
        priorityText = item.priority.toString(),
        revisionText = item.revision.toString(),
        locale = item.locale,
        archived = "${item.archived}",
        activeText = "${item.active}",
        category = truncate(item.category, 40),
        contactEmailText = item.contactEmail,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toBatchedLeaseReturnPolicyViews(items: MutableList<BatchedLeaseReturnPolicy>): MutableList<BatchedLeaseReturnPolicyView> {
    val views = mutableListOf<BatchedLeaseReturnPolicyView>()
    for (item in items) {
        views.add(toBatchedLeaseReturnPolicyView(item))
    }
    return views
}

fun sumBatchedLeaseReturnPolicyView(view: BatchedLeaseReturnPolicyView): Double {
    val parsedQuantity = view.quantity.toInt()
    val parsedPriority = view.priorityText.toInt()
    val parsedRevision = view.revisionText.toInt()
    return parsedQuantity.toDouble() + parsedPriority.toDouble() + parsedRevision.toDouble()
}
