package com.acme.transport.freightpay.freightinvoice.rollup

import com.acme.transport.kit.text.truncate

/** How screens and exports show a freight invoice quota. */
data class FreightInvoiceQuotaView(
    var code: String,
    var label: String,
    var endsAt: String,
    var approvedText: String,
    var notesText: String,
    var createdAt: String,
    var segment: String,
    var unit: String,
    var externalRef: String,
    var routeCodeText: String,
    var minimum: String,
    var statusLabel: String,
)

fun toFreightInvoiceQuotaView(item: FreightInvoiceQuota): FreightInvoiceQuotaView {
    return FreightInvoiceQuotaView(
        code = item.code,
        label = truncate(item.label, 64),
        endsAt = item.endsAt.toString(),
        approvedText = "${item.approved}",
        notesText = truncate(item.notes, 64),
        createdAt = item.createdAt.toString(),
        segment = truncate(item.segment, 64),
        unit = item.unit,
        externalRef = item.externalRef,
        routeCodeText = item.routeCode,
        minimum = "%.2f".format(item.minimum),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toFreightInvoiceQuotaViews(items: MutableList<FreightInvoiceQuota>): MutableList<FreightInvoiceQuotaView> {
    val views = mutableListOf<FreightInvoiceQuotaView>()
    for (item in items) {
        views.add(toFreightInvoiceQuotaView(item))
    }
    return views
}

fun sumFreightInvoiceQuotaView(view: FreightInvoiceQuotaView): Double {
    val parsedEndsAt = view.endsAt.toInt()
    val parsedCreatedAt = view.createdAt.toInt()
    val parsedMinimum = view.minimum.toDouble()
    return parsedEndsAt.toDouble() + parsedCreatedAt.toDouble() + parsedMinimum
}
