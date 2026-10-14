package com.acme.transport.lastmile.redelivery.ledger

import com.acme.transport.kit.text.truncate

/** How screens and exports show a global redelivery ledger. */
data class GlobalRedeliveryLedgerView(
    var codeText: String,
    var nameText: String,
    var score: String,
    var depotText: String,
    var threshold: String,
    var statusLabel: String,
)

fun toGlobalRedeliveryLedgerView(item: GlobalRedeliveryLedger): GlobalRedeliveryLedgerView {
    return GlobalRedeliveryLedgerView(
        codeText = truncate(item.code, 40),
        nameText = item.name,
        score = "%.2f".format(item.score),
        depotText = truncate(item.depot, 64),
        threshold = "%.2f".format(item.threshold),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toGlobalRedeliveryLedgerViews(items: MutableList<GlobalRedeliveryLedger>): MutableList<GlobalRedeliveryLedgerView> {
    val views = mutableListOf<GlobalRedeliveryLedgerView>()
    for (item in items) {
        views.add(toGlobalRedeliveryLedgerView(item))
    }
    return views
}

fun sumGlobalRedeliveryLedgerView(view: GlobalRedeliveryLedgerView): Double {
    val parsedScore = view.score.toDouble()
    val parsedThreshold = view.threshold.toDouble()
    return parsedScore + parsedThreshold
}
