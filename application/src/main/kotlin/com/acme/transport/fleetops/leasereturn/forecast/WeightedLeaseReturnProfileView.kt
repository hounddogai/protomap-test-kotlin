package com.acme.transport.fleetops.leasereturn.forecast

import com.acme.transport.kit.text.truncate

/** How screens and exports show a weighted lease return profile. */
data class WeightedLeaseReturnProfileView(
    var codeText: String,
    var label: String,
    var pinnedText: String,
    var updatedAt: String,
    var title: String,
    var vehicleId: String,
    var capacity: String,
    var region: String,
    var archived: String,
    var source: String,
    var statusLabel: String,
)

fun toWeightedLeaseReturnProfileView(item: WeightedLeaseReturnProfile): WeightedLeaseReturnProfileView {
    return WeightedLeaseReturnProfileView(
        codeText = truncate(item.code, 40),
        label = item.label,
        pinnedText = "${item.pinned}",
        updatedAt = item.updatedAt.toString(),
        title = item.title,
        vehicleId = item.vehicleId,
        capacity = item.capacity.toString(),
        region = item.region,
        archived = "${item.archived}",
        source = truncate(item.source, 80),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toWeightedLeaseReturnProfileViews(items: MutableList<WeightedLeaseReturnProfile>): MutableList<WeightedLeaseReturnProfileView> {
    val views = mutableListOf<WeightedLeaseReturnProfileView>()
    for (item in items) {
        views.add(toWeightedLeaseReturnProfileView(item))
    }
    return views
}

fun sumWeightedLeaseReturnProfileView(view: WeightedLeaseReturnProfileView): Double {
    val parsedUpdatedAt = view.updatedAt.toInt()
    val parsedCapacity = view.capacity.toInt()
    return parsedUpdatedAt.toDouble() + parsedCapacity.toDouble()
}
