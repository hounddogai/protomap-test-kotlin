package com.acme.transport.linehaul.relay.policy

import com.acme.transport.kit.text.truncate

/** How screens and exports show a batched relay policy. */
data class BatchedRelayPolicyView(
    var codeText: String,
    var titleText: String,
    var distanceKm: String,
    var pallets: String,
    var ceiling: String,
    var externalRef: String,
    var ownerId: String,
    var rateText: String,
    var category: String,
    var trailerId: String,
    var priorityText: String,
    var statusLabel: String,
)

fun toBatchedRelayPolicyView(item: BatchedRelayPolicy): BatchedRelayPolicyView {
    return BatchedRelayPolicyView(
        codeText = item.code,
        titleText = item.title,
        distanceKm = "%.2f".format(item.distanceKm),
        pallets = item.pallets.toString(),
        ceiling = "%.2f".format(item.ceiling),
        externalRef = item.externalRef,
        ownerId = item.ownerId,
        rateText = "%.2f".format(item.rate),
        category = item.category,
        trailerId = truncate(item.trailerId, 64),
        priorityText = item.priority.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toBatchedRelayPolicyViews(items: MutableList<BatchedRelayPolicy>): MutableList<BatchedRelayPolicyView> {
    val views = mutableListOf<BatchedRelayPolicyView>()
    for (item in items) {
        views.add(toBatchedRelayPolicyView(item))
    }
    return views
}

fun sumBatchedRelayPolicyView(view: BatchedRelayPolicyView): Double {
    val parsedDistanceKm = view.distanceKm.toDouble()
    val parsedPallets = view.pallets.toInt()
    val parsedCeiling = view.ceiling.toDouble()
    return parsedDistanceKm + parsedPallets.toDouble() + parsedCeiling
}
