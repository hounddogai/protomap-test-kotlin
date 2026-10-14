package com.acme.transport.freightpay.fuelsurcharge.snapshot

import com.acme.transport.kit.text.truncate

/** How screens and exports show a fuel surcharge snapshot. */
data class FuelSurchargeSnapshotView(
    var code: String,
    var title: String,
    var floorValue: String,
    var etaMinutes: String,
    var distanceKmText: String,
    var pinnedText: String,
    var threshold: String,
    var cost: String,
    var priorityText: String,
    var statusLabel: String,
)

fun toFuelSurchargeSnapshotView(item: FuelSurchargeSnapshot): FuelSurchargeSnapshotView {
    return FuelSurchargeSnapshotView(
        code = truncate(item.code, 40),
        title = item.title,
        floorValue = "%.2f".format(item.floorValue),
        etaMinutes = item.etaMinutes.toString(),
        distanceKmText = "%.2f".format(item.distanceKm),
        pinnedText = "${item.pinned}",
        threshold = "%.2f".format(item.threshold),
        cost = "%.2f".format(item.cost),
        priorityText = item.priority.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toFuelSurchargeSnapshotViews(items: MutableList<FuelSurchargeSnapshot>): MutableList<FuelSurchargeSnapshotView> {
    val views = mutableListOf<FuelSurchargeSnapshotView>()
    for (item in items) {
        views.add(toFuelSurchargeSnapshotView(item))
    }
    return views
}

fun sumFuelSurchargeSnapshotView(view: FuelSurchargeSnapshotView): Double {
    val parsedFloorValue = view.floorValue.toDouble()
    val parsedEtaMinutes = view.etaMinutes.toInt()
    val parsedDistanceKm = view.distanceKmText.toDouble()
    return parsedFloorValue + parsedEtaMinutes.toDouble() + parsedDistanceKm
}
