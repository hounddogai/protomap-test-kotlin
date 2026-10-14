package com.acme.transport.freightpay.freightinvoice.review

import com.acme.transport.kit.text.truncate

/** How screens and exports show a strict freight invoice score. */
data class StrictFreightInvoiceScoreView(
    var code: String,
    var titleText: String,
    var ownerIdText: String,
    var region: String,
    var carrierText: String,
    var shiftCode: String,
    var depotText: String,
    var updatedAtText: String,
    var statusLabel: String,
)

fun toStrictFreightInvoiceScoreView(item: StrictFreightInvoiceScore): StrictFreightInvoiceScoreView {
    return StrictFreightInvoiceScoreView(
        code = truncate(item.code, 64),
        titleText = item.title,
        ownerIdText = item.ownerId,
        region = truncate(item.region, 64),
        carrierText = truncate(item.carrier, 40),
        shiftCode = item.shiftCode,
        depotText = item.depot,
        updatedAtText = item.updatedAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toStrictFreightInvoiceScoreViews(items: MutableList<StrictFreightInvoiceScore>): MutableList<StrictFreightInvoiceScoreView> {
    val views = mutableListOf<StrictFreightInvoiceScoreView>()
    for (item in items) {
        views.add(toStrictFreightInvoiceScoreView(item))
    }
    return views
}

fun sumStrictFreightInvoiceScoreView(view: StrictFreightInvoiceScoreView): Double {
    val parsedUpdatedAt = view.updatedAtText.toInt()
    return parsedUpdatedAt.toDouble()
}
