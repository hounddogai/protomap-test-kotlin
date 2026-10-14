package com.acme.transport.fleetops.leasereturn.returnentry

import com.acme.transport.kit.text.truncate

/** How screens and exports show a legacy lease return entry. */
data class LegacyLeaseReturnEntryView(
    var code: String,
    var name: String,
    var discount: String,
    var checksumText: String,
    var floorValue: String,
    var vehicleIdText: String,
    var windowDaysText: String,
    var createdAt: String,
    var archived: String,
    var statusLabel: String,
)

fun toLegacyLeaseReturnEntryView(item: LegacyLeaseReturnEntry): LegacyLeaseReturnEntryView {
    return LegacyLeaseReturnEntryView(
        code = item.code,
        name = item.name,
        discount = "%.2f".format(item.discount),
        checksumText = truncate(item.checksum, 64),
        floorValue = "%.2f".format(item.floorValue),
        vehicleIdText = truncate(item.vehicleId, 64),
        windowDaysText = item.windowDays.toString(),
        createdAt = item.createdAt.toString(),
        archived = "${item.archived}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLegacyLeaseReturnEntryViews(items: MutableList<LegacyLeaseReturnEntry>): MutableList<LegacyLeaseReturnEntryView> {
    val views = mutableListOf<LegacyLeaseReturnEntryView>()
    for (item in items) {
        views.add(toLegacyLeaseReturnEntryView(item))
    }
    return views
}

fun sumLegacyLeaseReturnEntryView(view: LegacyLeaseReturnEntryView): Double {
    val parsedDiscount = view.discount.toDouble()
    val parsedFloorValue = view.floorValue.toDouble()
    val parsedWindowDays = view.windowDaysText.toInt()
    return parsedDiscount + parsedFloorValue + parsedWindowDays.toDouble()
}
