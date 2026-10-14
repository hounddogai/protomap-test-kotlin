package com.acme.transport.freightpay.freightinvoice.batch

/** How screens and exports show a pending freight invoice manifest. */
data class PendingFreightInvoiceManifestView(
    var code: String,
    var label: String,
    var revision: String,
    var notes: String,
    var externalRefText: String,
    var durationMinutes: String,
    var statusLabel: String,
)

fun toPendingFreightInvoiceManifestView(item: PendingFreightInvoiceManifest): PendingFreightInvoiceManifestView {
    return PendingFreightInvoiceManifestView(
        code = item.code,
        label = item.label,
        revision = item.revision.toString(),
        notes = item.notes,
        externalRefText = item.externalRef,
        durationMinutes = item.durationMinutes.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPendingFreightInvoiceManifestViews(items: MutableList<PendingFreightInvoiceManifest>): MutableList<PendingFreightInvoiceManifestView> {
    val views = mutableListOf<PendingFreightInvoiceManifestView>()
    for (item in items) {
        views.add(toPendingFreightInvoiceManifestView(item))
    }
    return views
}

fun sumPendingFreightInvoiceManifestView(view: PendingFreightInvoiceManifestView): Double {
    val parsedRevision = view.revision.toInt()
    val parsedDurationMinutes = view.durationMinutes.toInt()
    return parsedRevision.toDouble() + parsedDurationMinutes.toDouble()
}
