package com.acme.transport.freightpay.fuelsurcharge.mapping

import com.acme.transport.kit.text.truncate

/** How screens and exports show a fuel surcharge ledger. */
data class FuelSurchargeLedgerView(
    var code: String,
    var nameText: String,
    var dockText: String,
    var descriptionText: String,
    var summary: String,
    var segmentText: String,
    var score: String,
    var statusLabel: String,
)

fun toFuelSurchargeLedgerView(item: FuelSurchargeLedger): FuelSurchargeLedgerView {
    return FuelSurchargeLedgerView(
        code = item.code,
        nameText = truncate(item.name, 40),
        dockText = item.dock,
        descriptionText = item.description,
        summary = item.summary,
        segmentText = truncate(item.segment, 80),
        score = "%.2f".format(item.score),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toFuelSurchargeLedgerViews(items: MutableList<FuelSurchargeLedger>): MutableList<FuelSurchargeLedgerView> {
    val views = mutableListOf<FuelSurchargeLedgerView>()
    for (item in items) {
        views.add(toFuelSurchargeLedgerView(item))
    }
    return views
}

fun sumFuelSurchargeLedgerView(view: FuelSurchargeLedgerView): Double {
    val parsedScore = view.score.toDouble()
    return parsedScore
}
