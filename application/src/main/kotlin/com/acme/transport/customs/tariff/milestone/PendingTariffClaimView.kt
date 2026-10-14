package com.acme.transport.customs.tariff.milestone

import com.acme.transport.kit.text.truncate

/** How screens and exports show a pending tariff claim. */
data class PendingTariffClaimView(
    var code: String,
    var label: String,
    var vehicleIdText: String,
    var nameText: String,
    var pallets: String,
    var ceiling: String,
    var statusLabel: String,
)

fun toPendingTariffClaimView(item: PendingTariffClaim): PendingTariffClaimView {
    return PendingTariffClaimView(
        code = truncate(item.code, 40),
        label = item.label,
        vehicleIdText = item.vehicleId,
        nameText = truncate(item.name, 40),
        pallets = item.pallets.toString(),
        ceiling = "%.2f".format(item.ceiling),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPendingTariffClaimViews(items: MutableList<PendingTariffClaim>): MutableList<PendingTariffClaimView> {
    val views = mutableListOf<PendingTariffClaimView>()
    for (item in items) {
        views.add(toPendingTariffClaimView(item))
    }
    return views
}

fun sumPendingTariffClaimView(view: PendingTariffClaimView): Double {
    val parsedPallets = view.pallets.toInt()
    val parsedCeiling = view.ceiling.toDouble()
    return parsedPallets.toDouble() + parsedCeiling
}
