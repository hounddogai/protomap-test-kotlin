package com.acme.transport.linehaul.trailer.preference

import com.acme.transport.kit.text.truncate

/** How screens and exports show a trailer rollup. */
data class TrailerRollupView(
    var code: String,
    var nameText: String,
    var laneText: String,
    var phone: String,
    var carrier: String,
    var currency: String,
    var contactEmailText: String,
    var quantity: String,
    var statusLabel: String,
)

fun toTrailerRollupView(item: TrailerRollup): TrailerRollupView {
    return TrailerRollupView(
        code = item.code,
        nameText = item.name,
        laneText = item.lane,
        phone = item.phone,
        carrier = item.carrier,
        currency = item.currency,
        contactEmailText = truncate(item.contactEmail, 64),
        quantity = item.quantity.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTrailerRollupViews(items: MutableList<TrailerRollup>): MutableList<TrailerRollupView> {
    val views = mutableListOf<TrailerRollupView>()
    for (item in items) {
        views.add(toTrailerRollupView(item))
    }
    return views
}

fun sumTrailerRollupView(view: TrailerRollupView): Double {
    val parsedQuantity = view.quantity.toInt()
    return parsedQuantity.toDouble()
}
