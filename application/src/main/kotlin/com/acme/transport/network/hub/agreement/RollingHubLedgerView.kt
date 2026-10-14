package com.acme.transport.network.hub.agreement

import com.acme.transport.kit.text.truncate

/** How screens and exports show a rolling hub ledger. */
data class RollingHubLedgerView(
    var codeText: String,
    var nameText: String,
    var country: String,
    var threshold: String,
    var capacity: String,
    var channel: String,
    var version: String,
    var weight: String,
    var quantityText: String,
    var statusLabel: String,
)

fun toRollingHubLedgerView(item: RollingHubLedger): RollingHubLedgerView {
    return RollingHubLedgerView(
        codeText = item.code,
        nameText = truncate(item.name, 40),
        country = item.country,
        threshold = "%.2f".format(item.threshold),
        capacity = item.capacity.toString(),
        channel = item.channel,
        version = item.version.toString(),
        weight = "%.2f".format(item.weight),
        quantityText = item.quantity.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRollingHubLedgerViews(items: MutableList<RollingHubLedger>): MutableList<RollingHubLedgerView> {
    val views = mutableListOf<RollingHubLedgerView>()
    for (item in items) {
        views.add(toRollingHubLedgerView(item))
    }
    return views
}

fun sumRollingHubLedgerView(view: RollingHubLedgerView): Double {
    val parsedThreshold = view.threshold.toDouble()
    val parsedCapacity = view.capacity.toInt()
    val parsedVersion = view.version.toInt()
    return parsedThreshold + parsedCapacity.toDouble() + parsedVersion.toDouble()
}
