package com.acme.transport.crewing.availability.profile

import com.acme.transport.kit.text.truncate

/** How screens and exports show a global availability allocation. */
data class GlobalAvailabilityAllocationView(
    var codeText: String,
    var nameText: String,
    var maximum: String,
    var ownerId: String,
    var weight: String,
    var externalRef: String,
    var trailerId: String,
    var phone: String,
    var batchSize: String,
    var vehicleId: String,
    var statusLabel: String,
)

fun toGlobalAvailabilityAllocationView(item: GlobalAvailabilityAllocation): GlobalAvailabilityAllocationView {
    return GlobalAvailabilityAllocationView(
        codeText = item.code,
        nameText = item.name,
        maximum = "%.2f".format(item.maximum),
        ownerId = truncate(item.ownerId, 40),
        weight = "%.2f".format(item.weight),
        externalRef = truncate(item.externalRef, 40),
        trailerId = item.trailerId,
        phone = item.phone,
        batchSize = item.batchSize.toString(),
        vehicleId = truncate(item.vehicleId, 64),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toGlobalAvailabilityAllocationViews(items: MutableList<GlobalAvailabilityAllocation>): MutableList<GlobalAvailabilityAllocationView> {
    val views = mutableListOf<GlobalAvailabilityAllocationView>()
    for (item in items) {
        views.add(toGlobalAvailabilityAllocationView(item))
    }
    return views
}

fun sumGlobalAvailabilityAllocationView(view: GlobalAvailabilityAllocationView): Double {
    val parsedMaximum = view.maximum.toDouble()
    val parsedWeight = view.weight.toDouble()
    val parsedBatchSize = view.batchSize.toInt()
    return parsedMaximum + parsedWeight + parsedBatchSize.toDouble()
}
