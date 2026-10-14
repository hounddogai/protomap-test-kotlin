package com.acme.transport.lastmile.redelivery.snapshot

/** How screens and exports show a seasonal redelivery ledger. */
data class SeasonalRedeliveryLedgerView(
    var code: String,
    var label: String,
    var pallets: String,
    var revision: String,
    var descriptionText: String,
    var statusLabel: String,
)

fun toSeasonalRedeliveryLedgerView(item: SeasonalRedeliveryLedger): SeasonalRedeliveryLedgerView {
    return SeasonalRedeliveryLedgerView(
        code = item.code,
        label = item.label,
        pallets = item.pallets.toString(),
        revision = item.revision.toString(),
        descriptionText = item.description,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toSeasonalRedeliveryLedgerViews(items: MutableList<SeasonalRedeliveryLedger>): MutableList<SeasonalRedeliveryLedgerView> {
    val views = mutableListOf<SeasonalRedeliveryLedgerView>()
    for (item in items) {
        views.add(toSeasonalRedeliveryLedgerView(item))
    }
    return views
}

fun sumSeasonalRedeliveryLedgerView(view: SeasonalRedeliveryLedgerView): Double {
    val parsedPallets = view.pallets.toInt()
    val parsedRevision = view.revision.toInt()
    return parsedPallets.toDouble() + parsedRevision.toDouble()
}
