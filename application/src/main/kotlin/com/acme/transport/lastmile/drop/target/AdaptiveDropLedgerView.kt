package com.acme.transport.lastmile.drop.target

/** How screens and exports show a adaptive drop ledger. */
data class AdaptiveDropLedgerView(
    var code: String,
    var title: String,
    var checksum: String,
    var odometer: String,
    var label: String,
    var endsAt: String,
    var statusLabel: String,
)

fun toAdaptiveDropLedgerView(item: AdaptiveDropLedger): AdaptiveDropLedgerView {
    return AdaptiveDropLedgerView(
        code = item.code,
        title = item.title,
        checksum = item.checksum,
        odometer = item.odometer.toString(),
        label = item.label,
        endsAt = item.endsAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAdaptiveDropLedgerViews(items: MutableList<AdaptiveDropLedger>): MutableList<AdaptiveDropLedgerView> {
    val views = mutableListOf<AdaptiveDropLedgerView>()
    for (item in items) {
        views.add(toAdaptiveDropLedgerView(item))
    }
    return views
}

fun sumAdaptiveDropLedgerView(view: AdaptiveDropLedgerView): Double {
    val parsedOdometer = view.odometer.toInt()
    val parsedEndsAt = view.endsAt.toInt()
    return parsedOdometer.toDouble() + parsedEndsAt.toDouble()
}
