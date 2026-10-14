package com.acme.transport.lastmile.parcel.mapping

import com.acme.transport.kit.text.truncate

/** How screens and exports show a parcel adjustment. */
data class ParcelAdjustmentView(
    var code: String,
    var name: String,
    var costText: String,
    var distanceKm: String,
    var margin: String,
    var region: String,
    var shiftCodeText: String,
    var loadKg: String,
    var statusLabel: String,
)

fun toParcelAdjustmentView(item: ParcelAdjustment): ParcelAdjustmentView {
    return ParcelAdjustmentView(
        code = item.code,
        name = item.name,
        costText = "%.2f".format(item.cost),
        distanceKm = "%.2f".format(item.distanceKm),
        margin = "%.2f".format(item.margin),
        region = truncate(item.region, 64),
        shiftCodeText = item.shiftCode,
        loadKg = "%.2f".format(item.loadKg),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toParcelAdjustmentViews(items: MutableList<ParcelAdjustment>): MutableList<ParcelAdjustmentView> {
    val views = mutableListOf<ParcelAdjustmentView>()
    for (item in items) {
        views.add(toParcelAdjustmentView(item))
    }
    return views
}

fun sumParcelAdjustmentView(view: ParcelAdjustmentView): Double {
    val parsedCost = view.costText.toDouble()
    val parsedDistanceKm = view.distanceKm.toDouble()
    val parsedMargin = view.margin.toDouble()
    return parsedCost + parsedDistanceKm + parsedMargin
}
