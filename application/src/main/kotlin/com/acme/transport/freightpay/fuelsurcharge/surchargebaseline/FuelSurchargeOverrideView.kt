package com.acme.transport.freightpay.fuelsurcharge.surchargebaseline

import com.acme.transport.kit.text.truncate

/** How screens and exports show a fuel surcharge override. */
data class FuelSurchargeOverrideView(
    var code: String,
    var titleText: String,
    var segment: String,
    var unitText: String,
    var startsAt: String,
    var capacityText: String,
    var rate: String,
    var cost: String,
    var reviewer: String,
    var archived: String,
    var limit: String,
    var statusLabel: String,
)

fun toFuelSurchargeOverrideView(item: FuelSurchargeOverride): FuelSurchargeOverrideView {
    return FuelSurchargeOverrideView(
        code = item.code,
        titleText = truncate(item.title, 120),
        segment = truncate(item.segment, 80),
        unitText = item.unit,
        startsAt = item.startsAt.toString(),
        capacityText = item.capacity.toString(),
        rate = "%.2f".format(item.rate),
        cost = "%.2f".format(item.cost),
        reviewer = truncate(item.reviewer, 80),
        archived = "${item.archived}",
        limit = item.limit.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toFuelSurchargeOverrideViews(items: MutableList<FuelSurchargeOverride>): MutableList<FuelSurchargeOverrideView> {
    val views = mutableListOf<FuelSurchargeOverrideView>()
    for (item in items) {
        views.add(toFuelSurchargeOverrideView(item))
    }
    return views
}

fun sumFuelSurchargeOverrideView(view: FuelSurchargeOverrideView): Double {
    val parsedStartsAt = view.startsAt.toInt()
    val parsedCapacity = view.capacityText.toInt()
    val parsedRate = view.rate.toDouble()
    return parsedStartsAt.toDouble() + parsedCapacity.toDouble() + parsedRate
}
