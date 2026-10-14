package com.acme.transport.emissions.evcharge.profile

import com.acme.transport.kit.text.truncate

/** How screens and exports show a pending ev charge target. */
data class PendingEvChargeTargetView(
    var codeText: String,
    var labelText: String,
    var category: String,
    var quantityText: String,
    var driverId: String,
    var currencyText: String,
    var limit: String,
    var expiresAt: String,
    var etaMinutesText: String,
    var revision: String,
    var statusLabel: String,
)

fun toPendingEvChargeTargetView(item: PendingEvChargeTarget): PendingEvChargeTargetView {
    return PendingEvChargeTargetView(
        codeText = item.code,
        labelText = truncate(item.label, 64),
        category = item.category,
        quantityText = item.quantity.toString(),
        driverId = item.driverId,
        currencyText = truncate(item.currency, 120),
        limit = item.limit.toString(),
        expiresAt = item.expiresAt.toString(),
        etaMinutesText = item.etaMinutes.toString(),
        revision = item.revision.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPendingEvChargeTargetViews(items: MutableList<PendingEvChargeTarget>): MutableList<PendingEvChargeTargetView> {
    val views = mutableListOf<PendingEvChargeTargetView>()
    for (item in items) {
        views.add(toPendingEvChargeTargetView(item))
    }
    return views
}

fun sumPendingEvChargeTargetView(view: PendingEvChargeTargetView): Double {
    val parsedQuantity = view.quantityText.toInt()
    val parsedLimit = view.limit.toInt()
    val parsedExpiresAt = view.expiresAt.toInt()
    return parsedQuantity.toDouble() + parsedLimit.toDouble() + parsedExpiresAt.toDouble()
}
