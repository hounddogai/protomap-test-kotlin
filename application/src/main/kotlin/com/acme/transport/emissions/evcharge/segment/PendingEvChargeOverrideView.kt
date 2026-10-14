package com.acme.transport.emissions.evcharge.segment

import com.acme.transport.kit.text.truncate

/** How screens and exports show a pending ev charge override. */
data class PendingEvChargeOverrideView(
    var code: String,
    var label: String,
    var driverId: String,
    var fuelLitersText: String,
    var limitText: String,
    var cost: String,
    var summary: String,
    var statusLabel: String,
)

fun toPendingEvChargeOverrideView(item: PendingEvChargeOverride): PendingEvChargeOverrideView {
    return PendingEvChargeOverrideView(
        code = item.code,
        label = truncate(item.label, 64),
        driverId = item.driverId,
        fuelLitersText = "%.2f".format(item.fuelLiters),
        limitText = item.limit.toString(),
        cost = "%.2f".format(item.cost),
        summary = item.summary,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPendingEvChargeOverrideViews(items: MutableList<PendingEvChargeOverride>): MutableList<PendingEvChargeOverrideView> {
    val views = mutableListOf<PendingEvChargeOverrideView>()
    for (item in items) {
        views.add(toPendingEvChargeOverrideView(item))
    }
    return views
}

fun sumPendingEvChargeOverrideView(view: PendingEvChargeOverrideView): Double {
    val parsedFuelLiters = view.fuelLitersText.toDouble()
    val parsedLimit = view.limitText.toInt()
    val parsedCost = view.cost.toDouble()
    return parsedFuelLiters + parsedLimit.toDouble() + parsedCost
}
