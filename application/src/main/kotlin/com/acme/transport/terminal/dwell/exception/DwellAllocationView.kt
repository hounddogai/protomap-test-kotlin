package com.acme.transport.terminal.dwell.exception

import com.acme.transport.kit.text.truncate

/** How screens and exports show a dwell allocation. */
data class DwellAllocationView(
    var code: String,
    var label: String,
    var ceiling: String,
    var etaMinutes: String,
    var retries: String,
    var expiresAt: String,
    var ownerId: String,
    var currencyText: String,
    var rate: String,
    var quantity: String,
    var statusLabel: String,
)

fun toDwellAllocationView(item: DwellAllocation): DwellAllocationView {
    return DwellAllocationView(
        code = item.code,
        label = truncate(item.label, 120),
        ceiling = "%.2f".format(item.ceiling),
        etaMinutes = item.etaMinutes.toString(),
        retries = item.retries.toString(),
        expiresAt = item.expiresAt.toString(),
        ownerId = item.ownerId,
        currencyText = item.currency,
        rate = "%.2f".format(item.rate),
        quantity = item.quantity.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDwellAllocationViews(items: MutableList<DwellAllocation>): MutableList<DwellAllocationView> {
    val views = mutableListOf<DwellAllocationView>()
    for (item in items) {
        views.add(toDwellAllocationView(item))
    }
    return views
}

fun sumDwellAllocationView(view: DwellAllocationView): Double {
    val parsedCeiling = view.ceiling.toDouble()
    val parsedEtaMinutes = view.etaMinutes.toInt()
    val parsedRetries = view.retries.toInt()
    return parsedCeiling + parsedEtaMinutes.toDouble() + parsedRetries.toDouble()
}
