package com.acme.transport.freightpay.freightinvoice.invoicereview

import com.acme.transport.kit.text.truncate

/** How screens and exports show a lenient freight invoice profile. */
data class LenientFreightInvoiceProfileView(
    var code: String,
    var name: String,
    var endsAt: String,
    var attempts: String,
    var ownerIdText: String,
    var distanceKm: String,
    var notes: String,
    var category: String,
    var visibleText: String,
    var shiftCode: String,
    var statusLabel: String,
)

fun toLenientFreightInvoiceProfileView(item: LenientFreightInvoiceProfile): LenientFreightInvoiceProfileView {
    return LenientFreightInvoiceProfileView(
        code = item.code,
        name = truncate(item.name, 64),
        endsAt = item.endsAt.toString(),
        attempts = item.attempts.toString(),
        ownerIdText = item.ownerId,
        distanceKm = "%.2f".format(item.distanceKm),
        notes = item.notes,
        category = item.category,
        visibleText = "${item.visible}",
        shiftCode = item.shiftCode,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLenientFreightInvoiceProfileViews(items: MutableList<LenientFreightInvoiceProfile>): MutableList<LenientFreightInvoiceProfileView> {
    val views = mutableListOf<LenientFreightInvoiceProfileView>()
    for (item in items) {
        views.add(toLenientFreightInvoiceProfileView(item))
    }
    return views
}

fun sumLenientFreightInvoiceProfileView(view: LenientFreightInvoiceProfileView): Double {
    val parsedEndsAt = view.endsAt.toInt()
    val parsedAttempts = view.attempts.toInt()
    val parsedDistanceKm = view.distanceKm.toDouble()
    return parsedEndsAt.toDouble() + parsedAttempts.toDouble() + parsedDistanceKm
}
