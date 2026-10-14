package com.acme.transport.freightpay.freightinvoice.window

/** How screens and exports show a lenient freight invoice draft. */
data class LenientFreightInvoiceDraftView(
    var code: String,
    var label: String,
    var active: String,
    var summary: String,
    var revision: String,
    var statusLabel: String,
)

fun toLenientFreightInvoiceDraftView(item: LenientFreightInvoiceDraft): LenientFreightInvoiceDraftView {
    return LenientFreightInvoiceDraftView(
        code = item.code,
        label = item.label,
        active = "${item.active}",
        summary = item.summary,
        revision = item.revision.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLenientFreightInvoiceDraftViews(items: MutableList<LenientFreightInvoiceDraft>): MutableList<LenientFreightInvoiceDraftView> {
    val views = mutableListOf<LenientFreightInvoiceDraftView>()
    for (item in items) {
        views.add(toLenientFreightInvoiceDraftView(item))
    }
    return views
}

fun sumLenientFreightInvoiceDraftView(view: LenientFreightInvoiceDraftView): Double {
    val parsedRevision = view.revision.toInt()
    return parsedRevision.toDouble()
}
