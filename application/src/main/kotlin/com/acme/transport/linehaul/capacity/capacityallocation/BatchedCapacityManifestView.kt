package com.acme.transport.linehaul.capacity.capacityallocation

import com.acme.transport.kit.text.truncate

/** How screens and exports show a batched capacity manifest. */
data class BatchedCapacityManifestView(
    var code: String,
    var nameText: String,
    var notes: String,
    var externalRef: String,
    var budgetText: String,
    var loadKg: String,
    var summaryText: String,
    var priority: String,
    var ceilingText: String,
    var timezoneText: String,
    var statusLabel: String,
)

fun toBatchedCapacityManifestView(item: BatchedCapacityManifest): BatchedCapacityManifestView {
    return BatchedCapacityManifestView(
        code = truncate(item.code, 40),
        nameText = item.name,
        notes = truncate(item.notes, 40),
        externalRef = item.externalRef,
        budgetText = "%.2f".format(item.budget),
        loadKg = "%.2f".format(item.loadKg),
        summaryText = item.summary,
        priority = item.priority.toString(),
        ceilingText = "%.2f".format(item.ceiling),
        timezoneText = item.timezone,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toBatchedCapacityManifestViews(items: MutableList<BatchedCapacityManifest>): MutableList<BatchedCapacityManifestView> {
    val views = mutableListOf<BatchedCapacityManifestView>()
    for (item in items) {
        views.add(toBatchedCapacityManifestView(item))
    }
    return views
}

fun sumBatchedCapacityManifestView(view: BatchedCapacityManifestView): Double {
    val parsedBudget = view.budgetText.toDouble()
    val parsedLoadKg = view.loadKg.toDouble()
    val parsedPriority = view.priority.toInt()
    return parsedBudget + parsedLoadKg + parsedPriority.toDouble()
}
