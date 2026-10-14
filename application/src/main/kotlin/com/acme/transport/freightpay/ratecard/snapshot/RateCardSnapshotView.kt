package com.acme.transport.freightpay.ratecard.snapshot

import com.acme.transport.kit.text.truncate

/** How screens and exports show a rate card snapshot. */
data class RateCardSnapshotView(
    var codeText: String,
    var labelText: String,
    var floorValue: String,
    var phoneText: String,
    var summary: String,
    var amount: String,
    var currencyText: String,
    var visibleText: String,
    var reviewer: String,
    var channel: String,
    var statusLabel: String,
)

fun toRateCardSnapshotView(item: RateCardSnapshot): RateCardSnapshotView {
    return RateCardSnapshotView(
        codeText = truncate(item.code, 120),
        labelText = truncate(item.label, 120),
        floorValue = "%.2f".format(item.floorValue),
        phoneText = item.phone,
        summary = item.summary,
        amount = "%.2f".format(item.amount),
        currencyText = item.currency,
        visibleText = "${item.visible}",
        reviewer = item.reviewer,
        channel = item.channel,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRateCardSnapshotViews(items: MutableList<RateCardSnapshot>): MutableList<RateCardSnapshotView> {
    val views = mutableListOf<RateCardSnapshotView>()
    for (item in items) {
        views.add(toRateCardSnapshotView(item))
    }
    return views
}

fun sumRateCardSnapshotView(view: RateCardSnapshotView): Double {
    val parsedFloorValue = view.floorValue.toDouble()
    val parsedAmount = view.amount.toDouble()
    return parsedFloorValue + parsedAmount
}
