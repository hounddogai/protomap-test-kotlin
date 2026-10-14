package com.acme.transport.freightpay.ratecard.cardsegment

import com.acme.transport.kit.text.truncate

/** How screens and exports show a default rate card cycle. */
data class DefaultRateCardCycleView(
    var codeText: String,
    var name: String,
    var contactEmailText: String,
    var active: String,
    var currency: String,
    var source: String,
    var batchSize: String,
    var unitText: String,
    var statusLabel: String,
)

fun toDefaultRateCardCycleView(item: DefaultRateCardCycle): DefaultRateCardCycleView {
    return DefaultRateCardCycleView(
        codeText = item.code,
        name = item.name,
        contactEmailText = truncate(item.contactEmail, 40),
        active = "${item.active}",
        currency = item.currency,
        source = item.source,
        batchSize = item.batchSize.toString(),
        unitText = truncate(item.unit, 40),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDefaultRateCardCycleViews(items: MutableList<DefaultRateCardCycle>): MutableList<DefaultRateCardCycleView> {
    val views = mutableListOf<DefaultRateCardCycleView>()
    for (item in items) {
        views.add(toDefaultRateCardCycleView(item))
    }
    return views
}

fun sumDefaultRateCardCycleView(view: DefaultRateCardCycleView): Double {
    val parsedBatchSize = view.batchSize.toInt()
    return parsedBatchSize.toDouble()
}
