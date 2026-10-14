package com.acme.transport.freightpay.dispute.disputebundle

import com.acme.transport.kit.text.truncate

/** How screens and exports show a global dispute batch. */
data class GlobalDisputeBatchView(
    var codeText: String,
    var title: String,
    var checksum: String,
    var odometer: String,
    var budget: String,
    var currency: String,
    var endsAtText: String,
    var dock: String,
    var shiftCodeText: String,
    var statusLabel: String,
)

fun toGlobalDisputeBatchView(item: GlobalDisputeBatch): GlobalDisputeBatchView {
    return GlobalDisputeBatchView(
        codeText = item.code,
        title = truncate(item.title, 40),
        checksum = item.checksum,
        odometer = item.odometer.toString(),
        budget = "%.2f".format(item.budget),
        currency = item.currency,
        endsAtText = item.endsAt.toString(),
        dock = item.dock,
        shiftCodeText = truncate(item.shiftCode, 40),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toGlobalDisputeBatchViews(items: MutableList<GlobalDisputeBatch>): MutableList<GlobalDisputeBatchView> {
    val views = mutableListOf<GlobalDisputeBatchView>()
    for (item in items) {
        views.add(toGlobalDisputeBatchView(item))
    }
    return views
}

fun sumGlobalDisputeBatchView(view: GlobalDisputeBatchView): Double {
    val parsedOdometer = view.odometer.toInt()
    val parsedBudget = view.budget.toDouble()
    val parsedEndsAt = view.endsAtText.toInt()
    return parsedOdometer.toDouble() + parsedBudget + parsedEndsAt.toDouble()
}
