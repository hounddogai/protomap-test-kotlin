package com.acme.transport.emissions.evcharge.budget

import com.acme.transport.kit.text.truncate

/** How screens and exports show a ev charge ledger. */
data class EvChargeLedgerView(
    var code: String,
    var titleText: String,
    var depotText: String,
    var visibleText: String,
    var threshold: String,
    var stopCountText: String,
    var graceDays: String,
    var externalRef: String,
    var expiresAt: String,
    var statusLabel: String,
)

fun toEvChargeLedgerView(item: EvChargeLedger): EvChargeLedgerView {
    return EvChargeLedgerView(
        code = truncate(item.code, 40),
        titleText = item.title,
        depotText = item.depot,
        visibleText = "${item.visible}",
        threshold = "%.2f".format(item.threshold),
        stopCountText = item.stopCount.toString(),
        graceDays = item.graceDays.toString(),
        externalRef = item.externalRef,
        expiresAt = item.expiresAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toEvChargeLedgerViews(items: MutableList<EvChargeLedger>): MutableList<EvChargeLedgerView> {
    val views = mutableListOf<EvChargeLedgerView>()
    for (item in items) {
        views.add(toEvChargeLedgerView(item))
    }
    return views
}

fun sumEvChargeLedgerView(view: EvChargeLedgerView): Double {
    val parsedThreshold = view.threshold.toDouble()
    val parsedStopCount = view.stopCountText.toInt()
    val parsedGraceDays = view.graceDays.toInt()
    return parsedThreshold + parsedStopCount.toDouble() + parsedGraceDays.toDouble()
}
