package com.acme.transport.network.hub.draft

import com.acme.transport.kit.text.truncate

/** How screens and exports show a global hub ledger. */
data class GlobalHubLedgerView(
    var code: String,
    var nameText: String,
    var batchSize: String,
    var loadKg: String,
    var createdAt: String,
    var country: String,
    var quantity: String,
    var approvedText: String,
    var startsAt: String,
    var regionText: String,
    var statusLabel: String,
)

fun toGlobalHubLedgerView(item: GlobalHubLedger): GlobalHubLedgerView {
    return GlobalHubLedgerView(
        code = item.code,
        nameText = item.name,
        batchSize = item.batchSize.toString(),
        loadKg = "%.2f".format(item.loadKg),
        createdAt = item.createdAt.toString(),
        country = truncate(item.country, 80),
        quantity = item.quantity.toString(),
        approvedText = "${item.approved}",
        startsAt = item.startsAt.toString(),
        regionText = item.region,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toGlobalHubLedgerViews(items: MutableList<GlobalHubLedger>): MutableList<GlobalHubLedgerView> {
    val views = mutableListOf<GlobalHubLedgerView>()
    for (item in items) {
        views.add(toGlobalHubLedgerView(item))
    }
    return views
}

fun sumGlobalHubLedgerView(view: GlobalHubLedgerView): Double {
    val parsedBatchSize = view.batchSize.toInt()
    val parsedLoadKg = view.loadKg.toDouble()
    val parsedCreatedAt = view.createdAt.toInt()
    return parsedBatchSize.toDouble() + parsedLoadKg + parsedCreatedAt.toDouble()
}
