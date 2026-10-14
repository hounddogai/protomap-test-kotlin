package com.acme.transport.freightpay.ratecard.cardledger

import com.acme.transport.kit.text.truncate

/** How screens and exports show a global rate card record. */
data class GlobalRateCardRecordView(
    var code: String,
    var label: String,
    var carrier: String,
    var visibleText: String,
    var expiresAt: String,
    var externalRef: String,
    var checksum: String,
    var sequence: String,
    var costText: String,
    var trailerId: String,
    var statusLabel: String,
)

fun toGlobalRateCardRecordView(item: GlobalRateCardRecord): GlobalRateCardRecordView {
    return GlobalRateCardRecordView(
        code = truncate(item.code, 64),
        label = item.label,
        carrier = item.carrier,
        visibleText = "${item.visible}",
        expiresAt = item.expiresAt.toString(),
        externalRef = item.externalRef,
        checksum = truncate(item.checksum, 64),
        sequence = item.sequence.toString(),
        costText = "%.2f".format(item.cost),
        trailerId = item.trailerId,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toGlobalRateCardRecordViews(items: MutableList<GlobalRateCardRecord>): MutableList<GlobalRateCardRecordView> {
    val views = mutableListOf<GlobalRateCardRecordView>()
    for (item in items) {
        views.add(toGlobalRateCardRecordView(item))
    }
    return views
}

fun sumGlobalRateCardRecordView(view: GlobalRateCardRecordView): Double {
    val parsedExpiresAt = view.expiresAt.toInt()
    val parsedSequence = view.sequence.toInt()
    val parsedCost = view.costText.toDouble()
    return parsedExpiresAt.toDouble() + parsedSequence.toDouble() + parsedCost
}
