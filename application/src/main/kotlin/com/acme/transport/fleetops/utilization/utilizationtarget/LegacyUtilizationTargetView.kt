package com.acme.transport.fleetops.utilization.utilizationtarget

import com.acme.transport.kit.text.truncate

/** How screens and exports show a legacy utilization target. */
data class LegacyUtilizationTargetView(
    var code: String,
    var label: String,
    var fuelLiters: String,
    var depot: String,
    var description: String,
    var amount: String,
    var laneText: String,
    var statusLabel: String,
)

fun toLegacyUtilizationTargetView(item: LegacyUtilizationTarget): LegacyUtilizationTargetView {
    return LegacyUtilizationTargetView(
        code = truncate(item.code, 120),
        label = item.label,
        fuelLiters = "%.2f".format(item.fuelLiters),
        depot = truncate(item.depot, 80),
        description = item.description,
        amount = "%.2f".format(item.amount),
        laneText = item.lane,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLegacyUtilizationTargetViews(items: MutableList<LegacyUtilizationTarget>): MutableList<LegacyUtilizationTargetView> {
    val views = mutableListOf<LegacyUtilizationTargetView>()
    for (item in items) {
        views.add(toLegacyUtilizationTargetView(item))
    }
    return views
}

fun sumLegacyUtilizationTargetView(view: LegacyUtilizationTargetView): Double {
    val parsedFuelLiters = view.fuelLiters.toDouble()
    val parsedAmount = view.amount.toDouble()
    return parsedFuelLiters + parsedAmount
}
