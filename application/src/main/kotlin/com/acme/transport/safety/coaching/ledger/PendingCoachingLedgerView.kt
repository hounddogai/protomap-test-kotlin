package com.acme.transport.safety.coaching.ledger

import com.acme.transport.kit.text.truncate

/** How screens and exports show a pending coaching ledger. */
data class PendingCoachingLedgerView(
    var codeText: String,
    var titleText: String,
    var currency: String,
    var name: String,
    var unit: String,
    var discount: String,
    var endsAt: String,
    var statusLabel: String,
)

fun toPendingCoachingLedgerView(item: PendingCoachingLedger): PendingCoachingLedgerView {
    return PendingCoachingLedgerView(
        codeText = item.code,
        titleText = truncate(item.title, 120),
        currency = item.currency,
        name = item.name,
        unit = item.unit,
        discount = "%.2f".format(item.discount),
        endsAt = item.endsAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPendingCoachingLedgerViews(items: MutableList<PendingCoachingLedger>): MutableList<PendingCoachingLedgerView> {
    val views = mutableListOf<PendingCoachingLedgerView>()
    for (item in items) {
        views.add(toPendingCoachingLedgerView(item))
    }
    return views
}

fun sumPendingCoachingLedgerView(view: PendingCoachingLedgerView): Double {
    val parsedDiscount = view.discount.toDouble()
    val parsedEndsAt = view.endsAt.toInt()
    return parsedDiscount + parsedEndsAt.toDouble()
}
