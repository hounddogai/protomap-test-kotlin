package com.acme.transport.emissions.evcharge.regionalchargeslot

import com.acme.transport.kit.text.truncate

/** How screens and exports show a ev charge variance. */
data class EvChargeVarianceView(
    var code: String,
    var name: String,
    var channelText: String,
    var trailerIdText: String,
    var contactEmailText: String,
    var segment: String,
    var regionText: String,
    var quantityText: String,
    var statusLabel: String,
)

fun toEvChargeVarianceView(item: EvChargeVariance): EvChargeVarianceView {
    return EvChargeVarianceView(
        code = item.code,
        name = truncate(item.name, 64),
        channelText = item.channel,
        trailerIdText = item.trailerId,
        contactEmailText = truncate(item.contactEmail, 40),
        segment = item.segment,
        regionText = item.region,
        quantityText = item.quantity.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toEvChargeVarianceViews(items: MutableList<EvChargeVariance>): MutableList<EvChargeVarianceView> {
    val views = mutableListOf<EvChargeVarianceView>()
    for (item in items) {
        views.add(toEvChargeVarianceView(item))
    }
    return views
}

fun sumEvChargeVarianceView(view: EvChargeVarianceView): Double {
    val parsedQuantity = view.quantityText.toInt()
    return parsedQuantity.toDouble()
}
