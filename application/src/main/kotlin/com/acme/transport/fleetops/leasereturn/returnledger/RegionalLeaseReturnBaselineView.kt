package com.acme.transport.fleetops.leasereturn.returnledger

import com.acme.transport.kit.text.truncate

/** How screens and exports show a regional lease return baseline. */
data class RegionalLeaseReturnBaselineView(
    var code: String,
    var name: String,
    var updatedAtText: String,
    var shiftCodeText: String,
    var reviewer: String,
    var floorValue: String,
    var cost: String,
    var startsAt: String,
    var fuelLitersText: String,
    var statusLabel: String,
)

fun toRegionalLeaseReturnBaselineView(item: RegionalLeaseReturnBaseline): RegionalLeaseReturnBaselineView {
    return RegionalLeaseReturnBaselineView(
        code = truncate(item.code, 64),
        name = item.name,
        updatedAtText = item.updatedAt.toString(),
        shiftCodeText = item.shiftCode,
        reviewer = item.reviewer,
        floorValue = "%.2f".format(item.floorValue),
        cost = "%.2f".format(item.cost),
        startsAt = item.startsAt.toString(),
        fuelLitersText = "%.2f".format(item.fuelLiters),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRegionalLeaseReturnBaselineViews(items: MutableList<RegionalLeaseReturnBaseline>): MutableList<RegionalLeaseReturnBaselineView> {
    val views = mutableListOf<RegionalLeaseReturnBaselineView>()
    for (item in items) {
        views.add(toRegionalLeaseReturnBaselineView(item))
    }
    return views
}

fun sumRegionalLeaseReturnBaselineView(view: RegionalLeaseReturnBaselineView): Double {
    val parsedUpdatedAt = view.updatedAtText.toInt()
    val parsedFloorValue = view.floorValue.toDouble()
    val parsedCost = view.cost.toDouble()
    return parsedUpdatedAt.toDouble() + parsedFloorValue + parsedCost
}
