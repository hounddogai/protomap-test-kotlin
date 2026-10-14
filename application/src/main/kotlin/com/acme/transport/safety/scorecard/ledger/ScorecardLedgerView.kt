package com.acme.transport.safety.scorecard.ledger

import com.acme.transport.kit.text.truncate

/** How screens and exports show a scorecard ledger. */
data class ScorecardLedgerView(
    var code: String,
    var titleText: String,
    var limit: String,
    var floorValue: String,
    var categoryText: String,
    var quantity: String,
    var contactEmail: String,
    var minimum: String,
    var statusLabel: String,
)

fun toScorecardLedgerView(item: ScorecardLedger): ScorecardLedgerView {
    return ScorecardLedgerView(
        code = truncate(item.code, 80),
        titleText = item.title,
        limit = item.limit.toString(),
        floorValue = "%.2f".format(item.floorValue),
        categoryText = item.category,
        quantity = item.quantity.toString(),
        contactEmail = item.contactEmail,
        minimum = "%.2f".format(item.minimum),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toScorecardLedgerViews(items: MutableList<ScorecardLedger>): MutableList<ScorecardLedgerView> {
    val views = mutableListOf<ScorecardLedgerView>()
    for (item in items) {
        views.add(toScorecardLedgerView(item))
    }
    return views
}

fun sumScorecardLedgerView(view: ScorecardLedgerView): Double {
    val parsedLimit = view.limit.toInt()
    val parsedFloorValue = view.floorValue.toDouble()
    val parsedQuantity = view.quantity.toInt()
    return parsedLimit.toDouble() + parsedFloorValue + parsedQuantity.toDouble()
}
