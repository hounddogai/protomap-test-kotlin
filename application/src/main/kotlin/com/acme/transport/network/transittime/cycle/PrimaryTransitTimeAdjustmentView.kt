package com.acme.transport.network.transittime.cycle

import com.acme.transport.kit.text.truncate

/** How screens and exports show a primary transit time adjustment. */
data class PrimaryTransitTimeAdjustmentView(
    var code: String,
    var title: String,
    var channel: String,
    var segment: String,
    var shiftCode: String,
    var carrierText: String,
    var quantityText: String,
    var country: String,
    var statusLabel: String,
)

fun toPrimaryTransitTimeAdjustmentView(item: PrimaryTransitTimeAdjustment): PrimaryTransitTimeAdjustmentView {
    return PrimaryTransitTimeAdjustmentView(
        code = truncate(item.code, 80),
        title = truncate(item.title, 40),
        channel = item.channel,
        segment = item.segment,
        shiftCode = item.shiftCode,
        carrierText = item.carrier,
        quantityText = item.quantity.toString(),
        country = truncate(item.country, 120),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPrimaryTransitTimeAdjustmentViews(items: MutableList<PrimaryTransitTimeAdjustment>): MutableList<PrimaryTransitTimeAdjustmentView> {
    val views = mutableListOf<PrimaryTransitTimeAdjustmentView>()
    for (item in items) {
        views.add(toPrimaryTransitTimeAdjustmentView(item))
    }
    return views
}

fun sumPrimaryTransitTimeAdjustmentView(view: PrimaryTransitTimeAdjustmentView): Double {
    val parsedQuantity = view.quantityText.toInt()
    return parsedQuantity.toDouble()
}
