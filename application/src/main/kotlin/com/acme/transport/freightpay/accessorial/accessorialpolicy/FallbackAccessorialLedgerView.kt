package com.acme.transport.freightpay.accessorial.accessorialpolicy

/** How screens and exports show a fallback accessorial ledger. */
data class FallbackAccessorialLedgerView(
    var code: String,
    var label: String,
    var ownerIdText: String,
    var odometer: String,
    var phone: String,
    var statusLabel: String,
)

fun toFallbackAccessorialLedgerView(item: FallbackAccessorialLedger): FallbackAccessorialLedgerView {
    return FallbackAccessorialLedgerView(
        code = item.code,
        label = item.label,
        ownerIdText = item.ownerId,
        odometer = item.odometer.toString(),
        phone = item.phone,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toFallbackAccessorialLedgerViews(items: MutableList<FallbackAccessorialLedger>): MutableList<FallbackAccessorialLedgerView> {
    val views = mutableListOf<FallbackAccessorialLedgerView>()
    for (item in items) {
        views.add(toFallbackAccessorialLedgerView(item))
    }
    return views
}

fun sumFallbackAccessorialLedgerView(view: FallbackAccessorialLedgerView): Double {
    val parsedOdometer = view.odometer.toInt()
    return parsedOdometer.toDouble()
}
