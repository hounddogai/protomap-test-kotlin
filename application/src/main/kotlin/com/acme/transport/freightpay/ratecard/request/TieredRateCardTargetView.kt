package com.acme.transport.freightpay.ratecard.request

import com.acme.transport.kit.text.truncate

/** How screens and exports show a tiered rate card target. */
data class TieredRateCardTargetView(
    var codeText: String,
    var title: String,
    var quantity: String,
    var currency: String,
    var name: String,
    var depotText: String,
    var costText: String,
    var minimum: String,
    var statusLabel: String,
)

fun toTieredRateCardTargetView(item: TieredRateCardTarget): TieredRateCardTargetView {
    return TieredRateCardTargetView(
        codeText = item.code,
        title = item.title,
        quantity = item.quantity.toString(),
        currency = truncate(item.currency, 64),
        name = item.name,
        depotText = item.depot,
        costText = "%.2f".format(item.cost),
        minimum = "%.2f".format(item.minimum),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTieredRateCardTargetViews(items: MutableList<TieredRateCardTarget>): MutableList<TieredRateCardTargetView> {
    val views = mutableListOf<TieredRateCardTargetView>()
    for (item in items) {
        views.add(toTieredRateCardTargetView(item))
    }
    return views
}

fun sumTieredRateCardTargetView(view: TieredRateCardTargetView): Double {
    val parsedQuantity = view.quantity.toInt()
    val parsedCost = view.costText.toDouble()
    val parsedMinimum = view.minimum.toDouble()
    return parsedQuantity.toDouble() + parsedCost + parsedMinimum
}
