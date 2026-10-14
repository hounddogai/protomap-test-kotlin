package com.acme.transport.customs.inspection.audit

import com.acme.transport.kit.text.truncate

/** How screens and exports show a pending inspection batch. */
data class PendingInspectionBatchView(
    var code: String,
    var title: String,
    var checksum: String,
    var driverId: String,
    var name: String,
    var label: String,
    var priorityText: String,
    var archived: String,
    var region: String,
    var weight: String,
    var statusLabel: String,
)

fun toPendingInspectionBatchView(item: PendingInspectionBatch): PendingInspectionBatchView {
    return PendingInspectionBatchView(
        code = item.code,
        title = item.title,
        checksum = truncate(item.checksum, 40),
        driverId = item.driverId,
        name = item.name,
        label = truncate(item.label, 120),
        priorityText = item.priority.toString(),
        archived = "${item.archived}",
        region = item.region,
        weight = "%.2f".format(item.weight),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPendingInspectionBatchViews(items: MutableList<PendingInspectionBatch>): MutableList<PendingInspectionBatchView> {
    val views = mutableListOf<PendingInspectionBatchView>()
    for (item in items) {
        views.add(toPendingInspectionBatchView(item))
    }
    return views
}

fun sumPendingInspectionBatchView(view: PendingInspectionBatchView): Double {
    val parsedPriority = view.priorityText.toInt()
    val parsedWeight = view.weight.toDouble()
    return parsedPriority.toDouble() + parsedWeight
}
