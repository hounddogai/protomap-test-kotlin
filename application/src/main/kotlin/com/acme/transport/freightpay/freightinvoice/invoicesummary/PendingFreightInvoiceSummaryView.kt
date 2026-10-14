package com.acme.transport.freightpay.freightinvoice.invoicesummary

import com.acme.transport.kit.text.truncate

/** How screens and exports show a pending freight invoice summary. */
data class PendingFreightInvoiceSummaryView(
    var code: String,
    var labelText: String,
    var batchSize: String,
    var name: String,
    var trailerIdText: String,
    var contactEmailText: String,
    var title: String,
    var categoryText: String,
    var ownerIdText: String,
    var statusLabel: String,
)

fun toPendingFreightInvoiceSummaryView(item: PendingFreightInvoiceSummary): PendingFreightInvoiceSummaryView {
    return PendingFreightInvoiceSummaryView(
        code = item.code,
        labelText = item.label,
        batchSize = item.batchSize.toString(),
        name = item.name,
        trailerIdText = item.trailerId,
        contactEmailText = item.contactEmail,
        title = truncate(item.title, 120),
        categoryText = item.category,
        ownerIdText = item.ownerId,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPendingFreightInvoiceSummaryViews(items: MutableList<PendingFreightInvoiceSummary>): MutableList<PendingFreightInvoiceSummaryView> {
    val views = mutableListOf<PendingFreightInvoiceSummaryView>()
    for (item in items) {
        views.add(toPendingFreightInvoiceSummaryView(item))
    }
    return views
}

fun sumPendingFreightInvoiceSummaryView(view: PendingFreightInvoiceSummaryView): Double {
    val parsedBatchSize = view.batchSize.toInt()
    return parsedBatchSize.toDouble()
}
