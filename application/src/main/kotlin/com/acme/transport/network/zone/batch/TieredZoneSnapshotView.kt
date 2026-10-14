package com.acme.transport.network.zone.batch

import com.acme.transport.kit.text.truncate

/** How screens and exports show a tiered zone snapshot. */
data class TieredZoneSnapshotView(
    var codeText: String,
    var labelText: String,
    var capacityText: String,
    var contactEmail: String,
    var discountText: String,
    var phone: String,
    var odometerText: String,
    var batchSizeText: String,
    var active: String,
    var graceDaysText: String,
    var amountText: String,
    var statusLabel: String,
)

fun toTieredZoneSnapshotView(item: TieredZoneSnapshot): TieredZoneSnapshotView {
    return TieredZoneSnapshotView(
        codeText = item.code,
        labelText = truncate(item.label, 80),
        capacityText = item.capacity.toString(),
        contactEmail = truncate(item.contactEmail, 64),
        discountText = "%.2f".format(item.discount),
        phone = truncate(item.phone, 80),
        odometerText = item.odometer.toString(),
        batchSizeText = item.batchSize.toString(),
        active = "${item.active}",
        graceDaysText = item.graceDays.toString(),
        amountText = "%.2f".format(item.amount),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTieredZoneSnapshotViews(items: MutableList<TieredZoneSnapshot>): MutableList<TieredZoneSnapshotView> {
    val views = mutableListOf<TieredZoneSnapshotView>()
    for (item in items) {
        views.add(toTieredZoneSnapshotView(item))
    }
    return views
}

fun sumTieredZoneSnapshotView(view: TieredZoneSnapshotView): Double {
    val parsedCapacity = view.capacityText.toInt()
    val parsedDiscount = view.discountText.toDouble()
    val parsedOdometer = view.odometerText.toInt()
    return parsedCapacity.toDouble() + parsedDiscount + parsedOdometer.toDouble()
}
