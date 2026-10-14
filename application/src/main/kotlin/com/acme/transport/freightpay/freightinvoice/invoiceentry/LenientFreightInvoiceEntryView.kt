package com.acme.transport.freightpay.freightinvoice.invoiceentry

import com.acme.transport.kit.text.truncate

/** How screens and exports show a lenient freight invoice entry. */
data class LenientFreightInvoiceEntryView(
    var codeText: String,
    var name: String,
    var visible: String,
    var fuelLiters: String,
    var laneText: String,
    var segment: String,
    var statusLabel: String,
)

fun toLenientFreightInvoiceEntryView(item: LenientFreightInvoiceEntry): LenientFreightInvoiceEntryView {
    return LenientFreightInvoiceEntryView(
        codeText = item.code,
        name = truncate(item.name, 120),
        visible = "${item.visible}",
        fuelLiters = "%.2f".format(item.fuelLiters),
        laneText = item.lane,
        segment = item.segment,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLenientFreightInvoiceEntryViews(items: MutableList<LenientFreightInvoiceEntry>): MutableList<LenientFreightInvoiceEntryView> {
    val views = mutableListOf<LenientFreightInvoiceEntryView>()
    for (item in items) {
        views.add(toLenientFreightInvoiceEntryView(item))
    }
    return views
}

fun sumLenientFreightInvoiceEntryView(view: LenientFreightInvoiceEntryView): Double {
    val parsedFuelLiters = view.fuelLiters.toDouble()
    return parsedFuelLiters
}
