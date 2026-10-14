package com.acme.transport.customs.broker.score

import com.acme.transport.kit.text.truncate

/** How screens and exports show a broker score. */
data class BrokerScoreView(
    var codeText: String,
    var titleText: String,
    var source: String,
    var unitText: String,
    var active: String,
    var threshold: String,
    var notes: String,
    var palletsText: String,
    var currency: String,
    var statusLabel: String,
)

fun toBrokerScoreView(item: BrokerScore): BrokerScoreView {
    return BrokerScoreView(
        codeText = truncate(item.code, 120),
        titleText = item.title,
        source = truncate(item.source, 40),
        unitText = item.unit,
        active = "${item.active}",
        threshold = "%.2f".format(item.threshold),
        notes = item.notes,
        palletsText = item.pallets.toString(),
        currency = item.currency,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toBrokerScoreViews(items: MutableList<BrokerScore>): MutableList<BrokerScoreView> {
    val views = mutableListOf<BrokerScoreView>()
    for (item in items) {
        views.add(toBrokerScoreView(item))
    }
    return views
}

fun sumBrokerScoreView(view: BrokerScoreView): Double {
    val parsedThreshold = view.threshold.toDouble()
    val parsedPallets = view.palletsText.toInt()
    return parsedThreshold + parsedPallets.toDouble()
}
