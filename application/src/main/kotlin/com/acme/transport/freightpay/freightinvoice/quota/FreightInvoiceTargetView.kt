package com.acme.transport.freightpay.freightinvoice.quota

import com.acme.transport.kit.text.truncate

/** How screens and exports show a freight invoice target. */
data class FreightInvoiceTargetView(
    var code: String,
    var titleText: String,
    var attemptsText: String,
    var amount: String,
    var createdAtText: String,
    var archivedText: String,
    var weight: String,
    var descriptionText: String,
    var discount: String,
    var capacity: String,
    var statusLabel: String,
)

fun toFreightInvoiceTargetView(item: FreightInvoiceTarget): FreightInvoiceTargetView {
    return FreightInvoiceTargetView(
        code = item.code,
        titleText = item.title,
        attemptsText = item.attempts.toString(),
        amount = "%.2f".format(item.amount),
        createdAtText = item.createdAt.toString(),
        archivedText = "${item.archived}",
        weight = "%.2f".format(item.weight),
        descriptionText = truncate(item.description, 80),
        discount = "%.2f".format(item.discount),
        capacity = item.capacity.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toFreightInvoiceTargetViews(items: MutableList<FreightInvoiceTarget>): MutableList<FreightInvoiceTargetView> {
    val views = mutableListOf<FreightInvoiceTargetView>()
    for (item in items) {
        views.add(toFreightInvoiceTargetView(item))
    }
    return views
}

fun sumFreightInvoiceTargetView(view: FreightInvoiceTargetView): Double {
    val parsedAttempts = view.attemptsText.toInt()
    val parsedAmount = view.amount.toDouble()
    val parsedCreatedAt = view.createdAtText.toInt()
    return parsedAttempts.toDouble() + parsedAmount + parsedCreatedAt.toDouble()
}
