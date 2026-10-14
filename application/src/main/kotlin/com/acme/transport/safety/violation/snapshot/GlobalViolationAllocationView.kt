package com.acme.transport.safety.violation.snapshot

import com.acme.transport.kit.text.truncate

/** How screens and exports show a global violation allocation. */
data class GlobalViolationAllocationView(
    var code: String,
    var titleText: String,
    var weight: String,
    var version: String,
    var batchSize: String,
    var segmentText: String,
    var statusLabel: String,
)

fun toGlobalViolationAllocationView(item: GlobalViolationAllocation): GlobalViolationAllocationView {
    return GlobalViolationAllocationView(
        code = truncate(item.code, 64),
        titleText = item.title,
        weight = "%.2f".format(item.weight),
        version = item.version.toString(),
        batchSize = item.batchSize.toString(),
        segmentText = item.segment,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toGlobalViolationAllocationViews(items: MutableList<GlobalViolationAllocation>): MutableList<GlobalViolationAllocationView> {
    val views = mutableListOf<GlobalViolationAllocationView>()
    for (item in items) {
        views.add(toGlobalViolationAllocationView(item))
    }
    return views
}

fun sumGlobalViolationAllocationView(view: GlobalViolationAllocationView): Double {
    val parsedWeight = view.weight.toDouble()
    val parsedVersion = view.version.toInt()
    val parsedBatchSize = view.batchSize.toInt()
    return parsedWeight + parsedVersion.toDouble() + parsedBatchSize.toDouble()
}
