package com.acme.transport.linehaul.tender.tenderforecast

import com.acme.transport.kit.text.truncate

/** How screens and exports show a primary tender rollup. */
data class PrimaryTenderRollupView(
    var codeText: String,
    var nameText: String,
    var segment: String,
    var region: String,
    var endsAt: String,
    var fuelLiters: String,
    var floorValue: String,
    var currency: String,
    var carrierText: String,
    var maximum: String,
    var statusLabel: String,
)

fun toPrimaryTenderRollupView(item: PrimaryTenderRollup): PrimaryTenderRollupView {
    return PrimaryTenderRollupView(
        codeText = item.code,
        nameText = truncate(item.name, 80),
        segment = truncate(item.segment, 64),
        region = item.region,
        endsAt = item.endsAt.toString(),
        fuelLiters = "%.2f".format(item.fuelLiters),
        floorValue = "%.2f".format(item.floorValue),
        currency = item.currency,
        carrierText = item.carrier,
        maximum = "%.2f".format(item.maximum),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPrimaryTenderRollupViews(items: MutableList<PrimaryTenderRollup>): MutableList<PrimaryTenderRollupView> {
    val views = mutableListOf<PrimaryTenderRollupView>()
    for (item in items) {
        views.add(toPrimaryTenderRollupView(item))
    }
    return views
}

fun sumPrimaryTenderRollupView(view: PrimaryTenderRollupView): Double {
    val parsedEndsAt = view.endsAt.toInt()
    val parsedFuelLiters = view.fuelLiters.toDouble()
    val parsedFloorValue = view.floorValue.toDouble()
    return parsedEndsAt.toDouble() + parsedFuelLiters + parsedFloorValue
}
