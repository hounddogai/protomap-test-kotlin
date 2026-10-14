package com.acme.transport.emissions.carbonreport.rollup

import com.acme.transport.kit.text.truncate

/** How screens and exports show a pending carbon report window. */
data class PendingCarbonReportWindowView(
    var codeText: String,
    var nameText: String,
    var expiresAtText: String,
    var label: String,
    var checksum: String,
    var retries: String,
    var shiftCodeText: String,
    var ownerId: String,
    var quantity: String,
    var statusLabel: String,
)

fun toPendingCarbonReportWindowView(item: PendingCarbonReportWindow): PendingCarbonReportWindowView {
    return PendingCarbonReportWindowView(
        codeText = item.code,
        nameText = truncate(item.name, 64),
        expiresAtText = item.expiresAt.toString(),
        label = truncate(item.label, 40),
        checksum = truncate(item.checksum, 40),
        retries = item.retries.toString(),
        shiftCodeText = item.shiftCode,
        ownerId = truncate(item.ownerId, 120),
        quantity = item.quantity.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPendingCarbonReportWindowViews(items: MutableList<PendingCarbonReportWindow>): MutableList<PendingCarbonReportWindowView> {
    val views = mutableListOf<PendingCarbonReportWindowView>()
    for (item in items) {
        views.add(toPendingCarbonReportWindowView(item))
    }
    return views
}

fun sumPendingCarbonReportWindowView(view: PendingCarbonReportWindowView): Double {
    val parsedExpiresAt = view.expiresAtText.toInt()
    val parsedRetries = view.retries.toInt()
    val parsedQuantity = view.quantity.toInt()
    return parsedExpiresAt.toDouble() + parsedRetries.toDouble() + parsedQuantity.toDouble()
}
