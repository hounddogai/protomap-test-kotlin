package com.acme.transport.emissions.evcharge.chargeslot

import com.acme.transport.kit.text.truncate

/** How screens and exports show a ev charge policy. */
data class EvChargePolicyView(
    var codeText: String,
    var nameText: String,
    var endsAt: String,
    var trailerId: String,
    var floorValue: String,
    var minimumText: String,
    var amountText: String,
    var statusLabel: String,
)

fun toEvChargePolicyView(item: EvChargePolicy): EvChargePolicyView {
    return EvChargePolicyView(
        codeText = item.code,
        nameText = truncate(item.name, 80),
        endsAt = item.endsAt.toString(),
        trailerId = item.trailerId,
        floorValue = "%.2f".format(item.floorValue),
        minimumText = "%.2f".format(item.minimum),
        amountText = "%.2f".format(item.amount),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toEvChargePolicyViews(items: MutableList<EvChargePolicy>): MutableList<EvChargePolicyView> {
    val views = mutableListOf<EvChargePolicyView>()
    for (item in items) {
        views.add(toEvChargePolicyView(item))
    }
    return views
}

fun sumEvChargePolicyView(view: EvChargePolicyView): Double {
    val parsedEndsAt = view.endsAt.toInt()
    val parsedFloorValue = view.floorValue.toDouble()
    val parsedMinimum = view.minimumText.toDouble()
    return parsedEndsAt.toDouble() + parsedFloorValue + parsedMinimum
}
