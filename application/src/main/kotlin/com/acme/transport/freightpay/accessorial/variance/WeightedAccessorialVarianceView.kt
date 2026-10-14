package com.acme.transport.freightpay.accessorial.variance

import com.acme.transport.kit.text.truncate

/** How screens and exports show a weighted accessorial variance. */
data class WeightedAccessorialVarianceView(
    var code: String,
    var title: String,
    var routeCode: String,
    var source: String,
    var expiresAt: String,
    var unitText: String,
    var pallets: String,
    var amountText: String,
    var channelText: String,
    var durationMinutes: String,
    var quantityText: String,
    var statusLabel: String,
)

fun toWeightedAccessorialVarianceView(item: WeightedAccessorialVariance): WeightedAccessorialVarianceView {
    return WeightedAccessorialVarianceView(
        code = item.code,
        title = item.title,
        routeCode = item.routeCode,
        source = truncate(item.source, 64),
        expiresAt = item.expiresAt.toString(),
        unitText = item.unit,
        pallets = item.pallets.toString(),
        amountText = "%.2f".format(item.amount),
        channelText = item.channel,
        durationMinutes = item.durationMinutes.toString(),
        quantityText = item.quantity.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toWeightedAccessorialVarianceViews(items: MutableList<WeightedAccessorialVariance>): MutableList<WeightedAccessorialVarianceView> {
    val views = mutableListOf<WeightedAccessorialVarianceView>()
    for (item in items) {
        views.add(toWeightedAccessorialVarianceView(item))
    }
    return views
}

fun sumWeightedAccessorialVarianceView(view: WeightedAccessorialVarianceView): Double {
    val parsedExpiresAt = view.expiresAt.toInt()
    val parsedPallets = view.pallets.toInt()
    val parsedAmount = view.amountText.toDouble()
    return parsedExpiresAt.toDouble() + parsedPallets.toDouble() + parsedAmount
}
