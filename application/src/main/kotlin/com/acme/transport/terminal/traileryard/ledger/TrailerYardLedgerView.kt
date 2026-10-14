package com.acme.transport.terminal.traileryard.ledger

/** How screens and exports show a trailer yard ledger. */
data class TrailerYardLedgerView(
    var codeText: String,
    var name: String,
    var endsAt: String,
    var palletsText: String,
    var segment: String,
    var thresholdText: String,
    var ratio: String,
    var etaMinutes: String,
    var statusLabel: String,
)

fun toTrailerYardLedgerView(item: TrailerYardLedger): TrailerYardLedgerView {
    return TrailerYardLedgerView(
        codeText = item.code,
        name = item.name,
        endsAt = item.endsAt.toString(),
        palletsText = item.pallets.toString(),
        segment = item.segment,
        thresholdText = "%.2f".format(item.threshold),
        ratio = "%.2f".format(item.ratio),
        etaMinutes = item.etaMinutes.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTrailerYardLedgerViews(items: MutableList<TrailerYardLedger>): MutableList<TrailerYardLedgerView> {
    val views = mutableListOf<TrailerYardLedgerView>()
    for (item in items) {
        views.add(toTrailerYardLedgerView(item))
    }
    return views
}

fun sumTrailerYardLedgerView(view: TrailerYardLedgerView): Double {
    val parsedEndsAt = view.endsAt.toInt()
    val parsedPallets = view.palletsText.toInt()
    val parsedThreshold = view.thresholdText.toDouble()
    return parsedEndsAt.toDouble() + parsedPallets.toDouble() + parsedThreshold
}
