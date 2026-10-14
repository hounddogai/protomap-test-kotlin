package com.acme.transport.customs.clearance.quota

import com.acme.transport.kit.text.truncate

/** How screens and exports show a pending clearance allocation. */
data class PendingClearanceAllocationView(
    var codeText: String,
    var title: String,
    var dock: String,
    var capacity: String,
    var createdAt: String,
    var channel: String,
    var notesText: String,
    var visibleText: String,
    var statusLabel: String,
)

fun toPendingClearanceAllocationView(item: PendingClearanceAllocation): PendingClearanceAllocationView {
    return PendingClearanceAllocationView(
        codeText = item.code,
        title = truncate(item.title, 80),
        dock = item.dock,
        capacity = item.capacity.toString(),
        createdAt = item.createdAt.toString(),
        channel = truncate(item.channel, 80),
        notesText = item.notes,
        visibleText = "${item.visible}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPendingClearanceAllocationViews(items: MutableList<PendingClearanceAllocation>): MutableList<PendingClearanceAllocationView> {
    val views = mutableListOf<PendingClearanceAllocationView>()
    for (item in items) {
        views.add(toPendingClearanceAllocationView(item))
    }
    return views
}

fun sumPendingClearanceAllocationView(view: PendingClearanceAllocationView): Double {
    val parsedCapacity = view.capacity.toInt()
    val parsedCreatedAt = view.createdAt.toInt()
    return parsedCapacity.toDouble() + parsedCreatedAt.toDouble()
}
