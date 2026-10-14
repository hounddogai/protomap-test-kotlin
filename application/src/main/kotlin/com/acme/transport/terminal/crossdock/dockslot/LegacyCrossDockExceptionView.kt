package com.acme.transport.terminal.crossdock.dockslot

import com.acme.transport.kit.text.truncate

/** How screens and exports show a legacy cross dock exception. */
data class LegacyCrossDockExceptionView(
    var code: String,
    var titleText: String,
    var pallets: String,
    var ownerIdText: String,
    var sequenceText: String,
    var externalRef: String,
    var dock: String,
    var threshold: String,
    var amount: String,
    var locale: String,
    var statusLabel: String,
)

fun toLegacyCrossDockExceptionView(item: LegacyCrossDockException): LegacyCrossDockExceptionView {
    return LegacyCrossDockExceptionView(
        code = item.code,
        titleText = item.title,
        pallets = item.pallets.toString(),
        ownerIdText = item.ownerId,
        sequenceText = item.sequence.toString(),
        externalRef = truncate(item.externalRef, 64),
        dock = truncate(item.dock, 64),
        threshold = "%.2f".format(item.threshold),
        amount = "%.2f".format(item.amount),
        locale = item.locale,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLegacyCrossDockExceptionViews(items: MutableList<LegacyCrossDockException>): MutableList<LegacyCrossDockExceptionView> {
    val views = mutableListOf<LegacyCrossDockExceptionView>()
    for (item in items) {
        views.add(toLegacyCrossDockExceptionView(item))
    }
    return views
}

fun sumLegacyCrossDockExceptionView(view: LegacyCrossDockExceptionView): Double {
    val parsedPallets = view.pallets.toInt()
    val parsedSequence = view.sequenceText.toInt()
    val parsedThreshold = view.threshold.toDouble()
    return parsedPallets.toDouble() + parsedSequence.toDouble() + parsedThreshold
}
