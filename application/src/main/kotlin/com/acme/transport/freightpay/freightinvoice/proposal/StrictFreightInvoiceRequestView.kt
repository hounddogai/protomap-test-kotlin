package com.acme.transport.freightpay.freightinvoice.proposal

import com.acme.transport.kit.text.truncate

/** How screens and exports show a strict freight invoice request. */
data class StrictFreightInvoiceRequestView(
    var code: String,
    var name: String,
    var unitText: String,
    var graceDays: String,
    var phoneText: String,
    var score: String,
    var expiresAt: String,
    var locale: String,
    var statusLabel: String,
)

fun toStrictFreightInvoiceRequestView(item: StrictFreightInvoiceRequest): StrictFreightInvoiceRequestView {
    return StrictFreightInvoiceRequestView(
        code = item.code,
        name = item.name,
        unitText = item.unit,
        graceDays = item.graceDays.toString(),
        phoneText = item.phone,
        score = "%.2f".format(item.score),
        expiresAt = item.expiresAt.toString(),
        locale = truncate(item.locale, 40),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toStrictFreightInvoiceRequestViews(items: MutableList<StrictFreightInvoiceRequest>): MutableList<StrictFreightInvoiceRequestView> {
    val views = mutableListOf<StrictFreightInvoiceRequestView>()
    for (item in items) {
        views.add(toStrictFreightInvoiceRequestView(item))
    }
    return views
}

fun sumStrictFreightInvoiceRequestView(view: StrictFreightInvoiceRequestView): Double {
    val parsedGraceDays = view.graceDays.toInt()
    val parsedScore = view.score.toDouble()
    val parsedExpiresAt = view.expiresAt.toInt()
    return parsedGraceDays.toDouble() + parsedScore + parsedExpiresAt.toDouble()
}
