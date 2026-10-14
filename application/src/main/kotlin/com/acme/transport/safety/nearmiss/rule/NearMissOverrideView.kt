package com.acme.transport.safety.nearmiss.rule

import com.acme.transport.kit.text.truncate

/** How screens and exports show a near miss override. */
data class NearMissOverrideView(
    var codeText: String,
    var label: String,
    var active: String,
    var reviewerText: String,
    var ownerIdText: String,
    var quantity: String,
    var batchSize: String,
    var priority: String,
    var timezone: String,
    var statusLabel: String,
)

fun toNearMissOverrideView(item: NearMissOverride): NearMissOverrideView {
    return NearMissOverrideView(
        codeText = truncate(item.code, 120),
        label = item.label,
        active = "${item.active}",
        reviewerText = item.reviewer,
        ownerIdText = item.ownerId,
        quantity = item.quantity.toString(),
        batchSize = item.batchSize.toString(),
        priority = item.priority.toString(),
        timezone = truncate(item.timezone, 120),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toNearMissOverrideViews(items: MutableList<NearMissOverride>): MutableList<NearMissOverrideView> {
    val views = mutableListOf<NearMissOverrideView>()
    for (item in items) {
        views.add(toNearMissOverrideView(item))
    }
    return views
}

fun sumNearMissOverrideView(view: NearMissOverrideView): Double {
    val parsedQuantity = view.quantity.toInt()
    val parsedBatchSize = view.batchSize.toInt()
    val parsedPriority = view.priority.toInt()
    return parsedQuantity.toDouble() + parsedBatchSize.toDouble() + parsedPriority.toDouble()
}
