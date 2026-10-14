package com.acme.transport.customs.broker.entry

import com.acme.transport.kit.text.truncate

/** How screens and exports show a broker notice. */
data class BrokerNoticeView(
    var code: String,
    var label: String,
    var fuelLitersText: String,
    var quantity: String,
    var durationMinutes: String,
    var expiresAtText: String,
    var statusLabel: String,
)

fun toBrokerNoticeView(item: BrokerNotice): BrokerNoticeView {
    return BrokerNoticeView(
        code = truncate(item.code, 80),
        label = item.label,
        fuelLitersText = "%.2f".format(item.fuelLiters),
        quantity = item.quantity.toString(),
        durationMinutes = item.durationMinutes.toString(),
        expiresAtText = item.expiresAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toBrokerNoticeViews(items: MutableList<BrokerNotice>): MutableList<BrokerNoticeView> {
    val views = mutableListOf<BrokerNoticeView>()
    for (item in items) {
        views.add(toBrokerNoticeView(item))
    }
    return views
}

fun sumBrokerNoticeView(view: BrokerNoticeView): Double {
    val parsedFuelLiters = view.fuelLitersText.toDouble()
    val parsedQuantity = view.quantity.toInt()
    val parsedDurationMinutes = view.durationMinutes.toInt()
    return parsedFuelLiters + parsedQuantity.toDouble() + parsedDurationMinutes.toDouble()
}
