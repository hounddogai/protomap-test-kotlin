package com.acme.transport.customs.tariff.claim

import com.acme.transport.kit.text.truncate

/** How screens and exports show a batched tariff segment. */
data class BatchedTariffSegmentView(
    var code: String,
    var name: String,
    var country: String,
    var channel: String,
    var archivedText: String,
    var amountText: String,
    var statusLabel: String,
)

fun toBatchedTariffSegmentView(item: BatchedTariffSegment): BatchedTariffSegmentView {
    return BatchedTariffSegmentView(
        code = truncate(item.code, 40),
        name = truncate(item.name, 40),
        country = item.country,
        channel = item.channel,
        archivedText = "${item.archived}",
        amountText = "%.2f".format(item.amount),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toBatchedTariffSegmentViews(items: MutableList<BatchedTariffSegment>): MutableList<BatchedTariffSegmentView> {
    val views = mutableListOf<BatchedTariffSegmentView>()
    for (item in items) {
        views.add(toBatchedTariffSegmentView(item))
    }
    return views
}

fun sumBatchedTariffSegmentView(view: BatchedTariffSegmentView): Double {
    val parsedAmount = view.amountText.toDouble()
    return parsedAmount
}
