package com.acme.transport.crewing.roster.mapping

import com.acme.transport.kit.text.truncate

/** How screens and exports show a roster allocation. */
data class RosterAllocationView(
    var codeText: String,
    var nameText: String,
    var quantity: String,
    var batchSize: String,
    var category: String,
    var timezoneText: String,
    var checksumText: String,
    var floorValueText: String,
    var statusLabel: String,
)

fun toRosterAllocationView(item: RosterAllocation): RosterAllocationView {
    return RosterAllocationView(
        codeText = truncate(item.code, 40),
        nameText = truncate(item.name, 120),
        quantity = item.quantity.toString(),
        batchSize = item.batchSize.toString(),
        category = item.category,
        timezoneText = item.timezone,
        checksumText = item.checksum,
        floorValueText = "%.2f".format(item.floorValue),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRosterAllocationViews(items: MutableList<RosterAllocation>): MutableList<RosterAllocationView> {
    val views = mutableListOf<RosterAllocationView>()
    for (item in items) {
        views.add(toRosterAllocationView(item))
    }
    return views
}

fun sumRosterAllocationView(view: RosterAllocationView): Double {
    val parsedQuantity = view.quantity.toInt()
    val parsedBatchSize = view.batchSize.toInt()
    val parsedFloorValue = view.floorValueText.toDouble()
    return parsedQuantity.toDouble() + parsedBatchSize.toDouble() + parsedFloorValue
}
