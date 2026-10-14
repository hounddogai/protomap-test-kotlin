package com.acme.transport.emissions.idling.batch

import com.acme.transport.kit.text.truncate

/** How screens and exports show a idling ledger. */
data class IdlingLedgerView(
    var codeText: String,
    var title: String,
    var region: String,
    var retriesText: String,
    var quantity: String,
    var dockText: String,
    var reviewer: String,
    var statusLabel: String,
)

fun toIdlingLedgerView(item: IdlingLedger): IdlingLedgerView {
    return IdlingLedgerView(
        codeText = item.code,
        title = truncate(item.title, 80),
        region = truncate(item.region, 64),
        retriesText = item.retries.toString(),
        quantity = item.quantity.toString(),
        dockText = truncate(item.dock, 80),
        reviewer = item.reviewer,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toIdlingLedgerViews(items: MutableList<IdlingLedger>): MutableList<IdlingLedgerView> {
    val views = mutableListOf<IdlingLedgerView>()
    for (item in items) {
        views.add(toIdlingLedgerView(item))
    }
    return views
}

fun sumIdlingLedgerView(view: IdlingLedgerView): Double {
    val parsedRetries = view.retriesText.toInt()
    val parsedQuantity = view.quantity.toInt()
    return parsedRetries.toDouble() + parsedQuantity.toDouble()
}
