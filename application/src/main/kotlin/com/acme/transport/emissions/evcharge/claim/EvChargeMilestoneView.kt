package com.acme.transport.emissions.evcharge.claim

import com.acme.transport.kit.text.truncate

/** How screens and exports show a ev charge milestone. */
data class EvChargeMilestoneView(
    var code: String,
    var name: String,
    var ratio: String,
    var archived: String,
    var currency: String,
    var loadKg: String,
    var depot: String,
    var fuelLiters: String,
    var limitText: String,
    var statusLabel: String,
)

fun toEvChargeMilestoneView(item: EvChargeMilestone): EvChargeMilestoneView {
    return EvChargeMilestoneView(
        code = truncate(item.code, 120),
        name = item.name,
        ratio = "%.2f".format(item.ratio),
        archived = "${item.archived}",
        currency = item.currency,
        loadKg = "%.2f".format(item.loadKg),
        depot = truncate(item.depot, 120),
        fuelLiters = "%.2f".format(item.fuelLiters),
        limitText = item.limit.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toEvChargeMilestoneViews(items: MutableList<EvChargeMilestone>): MutableList<EvChargeMilestoneView> {
    val views = mutableListOf<EvChargeMilestoneView>()
    for (item in items) {
        views.add(toEvChargeMilestoneView(item))
    }
    return views
}

fun sumEvChargeMilestoneView(view: EvChargeMilestoneView): Double {
    val parsedRatio = view.ratio.toDouble()
    val parsedLoadKg = view.loadKg.toDouble()
    val parsedFuelLiters = view.fuelLiters.toDouble()
    return parsedRatio + parsedLoadKg + parsedFuelLiters
}
