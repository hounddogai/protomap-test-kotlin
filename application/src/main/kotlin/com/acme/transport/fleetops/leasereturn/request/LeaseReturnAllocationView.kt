package com.acme.transport.fleetops.leasereturn.request

import com.acme.transport.kit.text.truncate

/** How screens and exports show a lease return allocation. */
data class LeaseReturnAllocationView(
    var codeText: String,
    var title: String,
    var loadKg: String,
    var category: String,
    var nameText: String,
    var priorityText: String,
    var regionText: String,
    var externalRefText: String,
    var createdAtText: String,
    var endsAt: String,
    var locale: String,
    var statusLabel: String,
)

fun toLeaseReturnAllocationView(item: LeaseReturnAllocation): LeaseReturnAllocationView {
    return LeaseReturnAllocationView(
        codeText = item.code,
        title = item.title,
        loadKg = "%.2f".format(item.loadKg),
        category = item.category,
        nameText = item.name,
        priorityText = item.priority.toString(),
        regionText = item.region,
        externalRefText = item.externalRef,
        createdAtText = item.createdAt.toString(),
        endsAt = item.endsAt.toString(),
        locale = truncate(item.locale, 80),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLeaseReturnAllocationViews(items: MutableList<LeaseReturnAllocation>): MutableList<LeaseReturnAllocationView> {
    val views = mutableListOf<LeaseReturnAllocationView>()
    for (item in items) {
        views.add(toLeaseReturnAllocationView(item))
    }
    return views
}

fun sumLeaseReturnAllocationView(view: LeaseReturnAllocationView): Double {
    val parsedLoadKg = view.loadKg.toDouble()
    val parsedPriority = view.priorityText.toInt()
    val parsedCreatedAt = view.createdAtText.toInt()
    return parsedLoadKg + parsedPriority.toDouble() + parsedCreatedAt.toDouble()
}
