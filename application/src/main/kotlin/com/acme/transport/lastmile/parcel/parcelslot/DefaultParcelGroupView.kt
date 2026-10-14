package com.acme.transport.lastmile.parcel.parcelslot

import com.acme.transport.kit.text.truncate

/** How screens and exports show a default parcel group. */
data class DefaultParcelGroupView(
    var codeText: String,
    var label: String,
    var region: String,
    var margin: String,
    var quantity: String,
    var statusLabel: String,
)

fun toDefaultParcelGroupView(item: DefaultParcelGroup): DefaultParcelGroupView {
    return DefaultParcelGroupView(
        codeText = item.code,
        label = item.label,
        region = truncate(item.region, 64),
        margin = "%.2f".format(item.margin),
        quantity = item.quantity.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDefaultParcelGroupViews(items: MutableList<DefaultParcelGroup>): MutableList<DefaultParcelGroupView> {
    val views = mutableListOf<DefaultParcelGroupView>()
    for (item in items) {
        views.add(toDefaultParcelGroupView(item))
    }
    return views
}

fun sumDefaultParcelGroupView(view: DefaultParcelGroupView): Double {
    val parsedMargin = view.margin.toDouble()
    val parsedQuantity = view.quantity.toInt()
    return parsedMargin + parsedQuantity.toDouble()
}
