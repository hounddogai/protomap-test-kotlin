package com.acme.transport.network.transittime.bundle

import com.acme.transport.kit.text.truncate

/** How screens and exports show a transit time exception. */
data class TransitTimeExceptionView(
    var code: String,
    var label: String,
    var currencyText: String,
    var approved: String,
    var carrier: String,
    var checksumText: String,
    var batchSize: String,
    var shiftCode: String,
    var statusLabel: String,
)

fun toTransitTimeExceptionView(item: TransitTimeException): TransitTimeExceptionView {
    return TransitTimeExceptionView(
        code = truncate(item.code, 80),
        label = item.label,
        currencyText = item.currency,
        approved = "${item.approved}",
        carrier = item.carrier,
        checksumText = item.checksum,
        batchSize = item.batchSize.toString(),
        shiftCode = item.shiftCode,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTransitTimeExceptionViews(items: MutableList<TransitTimeException>): MutableList<TransitTimeExceptionView> {
    val views = mutableListOf<TransitTimeExceptionView>()
    for (item in items) {
        views.add(toTransitTimeExceptionView(item))
    }
    return views
}

fun sumTransitTimeExceptionView(view: TransitTimeExceptionView): Double {
    val parsedBatchSize = view.batchSize.toInt()
    return parsedBatchSize.toDouble()
}
