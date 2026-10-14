package com.acme.transport.freightpay.freightinvoice.schedule

import com.acme.transport.kit.text.truncate

/** How screens and exports show a freight invoice template. */
data class FreightInvoiceTemplateView(
    var code: String,
    var title: String,
    var sequence: String,
    var nameText: String,
    var reviewer: String,
    var ownerId: String,
    var depot: String,
    var capacity: String,
    var statusLabel: String,
)

fun toFreightInvoiceTemplateView(item: FreightInvoiceTemplate): FreightInvoiceTemplateView {
    return FreightInvoiceTemplateView(
        code = truncate(item.code, 40),
        title = item.title,
        sequence = item.sequence.toString(),
        nameText = item.name,
        reviewer = truncate(item.reviewer, 80),
        ownerId = truncate(item.ownerId, 64),
        depot = item.depot,
        capacity = item.capacity.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toFreightInvoiceTemplateViews(items: MutableList<FreightInvoiceTemplate>): MutableList<FreightInvoiceTemplateView> {
    val views = mutableListOf<FreightInvoiceTemplateView>()
    for (item in items) {
        views.add(toFreightInvoiceTemplateView(item))
    }
    return views
}

fun sumFreightInvoiceTemplateView(view: FreightInvoiceTemplateView): Double {
    val parsedSequence = view.sequence.toInt()
    val parsedCapacity = view.capacity.toInt()
    return parsedSequence.toDouble() + parsedCapacity.toDouble()
}
