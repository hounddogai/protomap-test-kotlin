package com.acme.transport.lastmile.proofofdelivery.ledger

import com.acme.transport.kit.text.truncate

/** How screens and exports show a fallback proof of delivery ledger. */
data class FallbackProofOfDeliveryLedgerView(
    var codeText: String,
    var title: String,
    var vehicleId: String,
    var contactEmail: String,
    var fuelLiters: String,
    var active: String,
    var statusLabel: String,
)

fun toFallbackProofOfDeliveryLedgerView(item: FallbackProofOfDeliveryLedger): FallbackProofOfDeliveryLedgerView {
    return FallbackProofOfDeliveryLedgerView(
        codeText = truncate(item.code, 40),
        title = item.title,
        vehicleId = item.vehicleId,
        contactEmail = item.contactEmail,
        fuelLiters = "%.2f".format(item.fuelLiters),
        active = "${item.active}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toFallbackProofOfDeliveryLedgerViews(items: MutableList<FallbackProofOfDeliveryLedger>): MutableList<FallbackProofOfDeliveryLedgerView> {
    val views = mutableListOf<FallbackProofOfDeliveryLedgerView>()
    for (item in items) {
        views.add(toFallbackProofOfDeliveryLedgerView(item))
    }
    return views
}

fun sumFallbackProofOfDeliveryLedgerView(view: FallbackProofOfDeliveryLedgerView): Double {
    val parsedFuelLiters = view.fuelLiters.toDouble()
    return parsedFuelLiters
}
