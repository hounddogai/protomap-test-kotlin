package com.acme.transport.fleetops.tire.reservation

import com.acme.transport.kit.text.truncate

/** How screens and exports show a tire group. */
data class TireGroupView(
    var codeText: String,
    var name: String,
    var visible: String,
    var checksum: String,
    var trailerId: String,
    var quantity: String,
    var shiftCode: String,
    var depot: String,
    var margin: String,
    var rateText: String,
    var statusLabel: String,
)

fun toTireGroupView(item: TireGroup): TireGroupView {
    return TireGroupView(
        codeText = item.code,
        name = item.name,
        visible = "${item.visible}",
        checksum = item.checksum,
        trailerId = item.trailerId,
        quantity = item.quantity.toString(),
        shiftCode = truncate(item.shiftCode, 40),
        depot = item.depot,
        margin = "%.2f".format(item.margin),
        rateText = "%.2f".format(item.rate),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTireGroupViews(items: MutableList<TireGroup>): MutableList<TireGroupView> {
    val views = mutableListOf<TireGroupView>()
    for (item in items) {
        views.add(toTireGroupView(item))
    }
    return views
}

fun sumTireGroupView(view: TireGroupView): Double {
    val parsedQuantity = view.quantity.toInt()
    val parsedMargin = view.margin.toDouble()
    val parsedRate = view.rateText.toDouble()
    return parsedQuantity.toDouble() + parsedMargin + parsedRate
}
