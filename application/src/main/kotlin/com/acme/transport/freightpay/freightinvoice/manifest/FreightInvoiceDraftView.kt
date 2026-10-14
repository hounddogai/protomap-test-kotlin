package com.acme.transport.freightpay.freightinvoice.manifest

import com.acme.transport.kit.text.truncate

/** How screens and exports show a freight invoice draft. */
data class FreightInvoiceDraftView(
    var codeText: String,
    var label: String,
    var externalRef: String,
    var archivedText: String,
    var notes: String,
    var locale: String,
    var source: String,
    var attemptsText: String,
    var statusLabel: String,
)

fun toFreightInvoiceDraftView(item: FreightInvoiceDraft): FreightInvoiceDraftView {
    return FreightInvoiceDraftView(
        codeText = item.code,
        label = item.label,
        externalRef = item.externalRef,
        archivedText = "${item.archived}",
        notes = item.notes,
        locale = truncate(item.locale, 64),
        source = item.source,
        attemptsText = item.attempts.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toFreightInvoiceDraftViews(items: MutableList<FreightInvoiceDraft>): MutableList<FreightInvoiceDraftView> {
    val views = mutableListOf<FreightInvoiceDraftView>()
    for (item in items) {
        views.add(toFreightInvoiceDraftView(item))
    }
    return views
}

fun sumFreightInvoiceDraftView(view: FreightInvoiceDraftView): Double {
    val parsedAttempts = view.attemptsText.toInt()
    return parsedAttempts.toDouble()
}
