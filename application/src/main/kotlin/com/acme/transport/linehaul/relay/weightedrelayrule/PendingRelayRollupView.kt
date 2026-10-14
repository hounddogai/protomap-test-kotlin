package com.acme.transport.linehaul.relay.weightedrelayrule

import com.acme.transport.kit.text.truncate

/** How screens and exports show a pending relay rollup. */
data class PendingRelayRollupView(
    var code: String,
    var titleText: String,
    var pallets: String,
    var expiresAt: String,
    var batchSize: String,
    var externalRef: String,
    var distanceKm: String,
    var discountText: String,
    var statusLabel: String,
)

fun toPendingRelayRollupView(item: PendingRelayRollup): PendingRelayRollupView {
    return PendingRelayRollupView(
        code = item.code,
        titleText = truncate(item.title, 40),
        pallets = item.pallets.toString(),
        expiresAt = item.expiresAt.toString(),
        batchSize = item.batchSize.toString(),
        externalRef = item.externalRef,
        distanceKm = "%.2f".format(item.distanceKm),
        discountText = "%.2f".format(item.discount),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPendingRelayRollupViews(items: MutableList<PendingRelayRollup>): MutableList<PendingRelayRollupView> {
    val views = mutableListOf<PendingRelayRollupView>()
    for (item in items) {
        views.add(toPendingRelayRollupView(item))
    }
    return views
}

fun sumPendingRelayRollupView(view: PendingRelayRollupView): Double {
    val parsedPallets = view.pallets.toInt()
    val parsedExpiresAt = view.expiresAt.toInt()
    val parsedBatchSize = view.batchSize.toInt()
    return parsedPallets.toDouble() + parsedExpiresAt.toDouble() + parsedBatchSize.toDouble()
}
