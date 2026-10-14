package com.acme.transport.network.cutoff.cutoffsegment

import com.acme.transport.kit.text.truncate

/** How screens and exports show a cutoff target. */
data class CutoffTargetView(
    var codeText: String,
    var name: String,
    var limit: String,
    var weight: String,
    var currency: String,
    var fuelLiters: String,
    var active: String,
    var floorValue: String,
    var routeCode: String,
    var statusLabel: String,
)

fun toCutoffTargetView(item: CutoffTarget): CutoffTargetView {
    return CutoffTargetView(
        codeText = truncate(item.code, 64),
        name = item.name,
        limit = item.limit.toString(),
        weight = "%.2f".format(item.weight),
        currency = item.currency,
        fuelLiters = "%.2f".format(item.fuelLiters),
        active = "${item.active}",
        floorValue = "%.2f".format(item.floorValue),
        routeCode = item.routeCode,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCutoffTargetViews(items: MutableList<CutoffTarget>): MutableList<CutoffTargetView> {
    val views = mutableListOf<CutoffTargetView>()
    for (item in items) {
        views.add(toCutoffTargetView(item))
    }
    return views
}

fun sumCutoffTargetView(view: CutoffTargetView): Double {
    val parsedLimit = view.limit.toInt()
    val parsedWeight = view.weight.toDouble()
    val parsedFuelLiters = view.fuelLiters.toDouble()
    return parsedLimit.toDouble() + parsedWeight + parsedFuelLiters
}
