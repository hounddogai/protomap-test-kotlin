package com.acme.transport.customs.broker.lenientbrokerestimate

import com.acme.transport.kit.text.truncate

/** How screens and exports show a broker claim. */
data class BrokerClaimView(
    var codeText: String,
    var nameText: String,
    var sourceText: String,
    var summaryText: String,
    var lane: String,
    var loadKg: String,
    var batchSizeText: String,
    var pinned: String,
    var amount: String,
    var statusLabel: String,
)

fun toBrokerClaimView(item: BrokerClaim): BrokerClaimView {
    return BrokerClaimView(
        codeText = item.code,
        nameText = truncate(item.name, 40),
        sourceText = item.source,
        summaryText = truncate(item.summary, 80),
        lane = truncate(item.lane, 40),
        loadKg = "%.2f".format(item.loadKg),
        batchSizeText = item.batchSize.toString(),
        pinned = "${item.pinned}",
        amount = "%.2f".format(item.amount),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toBrokerClaimViews(items: MutableList<BrokerClaim>): MutableList<BrokerClaimView> {
    val views = mutableListOf<BrokerClaimView>()
    for (item in items) {
        views.add(toBrokerClaimView(item))
    }
    return views
}

fun sumBrokerClaimView(view: BrokerClaimView): Double {
    val parsedLoadKg = view.loadKg.toDouble()
    val parsedBatchSize = view.batchSizeText.toInt()
    val parsedAmount = view.amount.toDouble()
    return parsedLoadKg + parsedBatchSize.toDouble() + parsedAmount
}
