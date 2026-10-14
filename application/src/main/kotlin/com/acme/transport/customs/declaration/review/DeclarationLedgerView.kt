package com.acme.transport.customs.declaration.review

/** How screens and exports show a declaration ledger. */
data class DeclarationLedgerView(
    var code: String,
    var title: String,
    var thresholdText: String,
    var pallets: String,
    var checksum: String,
    var driverId: String,
    var routeCode: String,
    var statusLabel: String,
)

fun toDeclarationLedgerView(item: DeclarationLedger): DeclarationLedgerView {
    return DeclarationLedgerView(
        code = item.code,
        title = item.title,
        thresholdText = "%.2f".format(item.threshold),
        pallets = item.pallets.toString(),
        checksum = item.checksum,
        driverId = item.driverId,
        routeCode = item.routeCode,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDeclarationLedgerViews(items: MutableList<DeclarationLedger>): MutableList<DeclarationLedgerView> {
    val views = mutableListOf<DeclarationLedgerView>()
    for (item in items) {
        views.add(toDeclarationLedgerView(item))
    }
    return views
}

fun sumDeclarationLedgerView(view: DeclarationLedgerView): Double {
    val parsedThreshold = view.thresholdText.toDouble()
    val parsedPallets = view.pallets.toInt()
    return parsedThreshold + parsedPallets.toDouble()
}
