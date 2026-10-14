package com.acme.transport.freightpay.freightinvoice.profile

import com.acme.transport.kit.text.truncate

/** How screens and exports show a freight invoice profile. */
data class FreightInvoiceProfileView(
    var codeText: String,
    var title: String,
    var durationMinutes: String,
    var ownerId: String,
    var depotText: String,
    var trailerIdText: String,
    var statusLabel: String,
)

fun toFreightInvoiceProfileView(item: FreightInvoiceProfile): FreightInvoiceProfileView {
    return FreightInvoiceProfileView(
        codeText = truncate(item.code, 40),
        title = item.title,
        durationMinutes = item.durationMinutes.toString(),
        ownerId = item.ownerId,
        depotText = item.depot,
        trailerIdText = item.trailerId,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toFreightInvoiceProfileViews(items: MutableList<FreightInvoiceProfile>): MutableList<FreightInvoiceProfileView> {
    val views = mutableListOf<FreightInvoiceProfileView>()
    for (item in items) {
        views.add(toFreightInvoiceProfileView(item))
    }
    return views
}

fun sumFreightInvoiceProfileView(view: FreightInvoiceProfileView): Double {
    val parsedDurationMinutes = view.durationMinutes.toInt()
    return parsedDurationMinutes.toDouble()
}
