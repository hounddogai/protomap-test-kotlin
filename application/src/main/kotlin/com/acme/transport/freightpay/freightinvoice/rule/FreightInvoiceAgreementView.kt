package com.acme.transport.freightpay.freightinvoice.rule

import com.acme.transport.kit.text.truncate

/** How screens and exports show a freight invoice agreement. */
data class FreightInvoiceAgreementView(
    var codeText: String,
    var name: String,
    var floorValue: String,
    var attempts: String,
    var carrier: String,
    var categoryText: String,
    var statusLabel: String,
)

fun toFreightInvoiceAgreementView(item: FreightInvoiceAgreement): FreightInvoiceAgreementView {
    return FreightInvoiceAgreementView(
        codeText = truncate(item.code, 64),
        name = item.name,
        floorValue = "%.2f".format(item.floorValue),
        attempts = item.attempts.toString(),
        carrier = truncate(item.carrier, 120),
        categoryText = truncate(item.category, 80),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toFreightInvoiceAgreementViews(items: MutableList<FreightInvoiceAgreement>): MutableList<FreightInvoiceAgreementView> {
    val views = mutableListOf<FreightInvoiceAgreementView>()
    for (item in items) {
        views.add(toFreightInvoiceAgreementView(item))
    }
    return views
}

fun sumFreightInvoiceAgreementView(view: FreightInvoiceAgreementView): Double {
    val parsedFloorValue = view.floorValue.toDouble()
    val parsedAttempts = view.attempts.toInt()
    return parsedFloorValue + parsedAttempts.toDouble()
}
