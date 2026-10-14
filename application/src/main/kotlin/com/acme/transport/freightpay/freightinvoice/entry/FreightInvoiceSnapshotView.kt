package com.acme.transport.freightpay.freightinvoice.entry

/** How screens and exports show a freight invoice snapshot. */
data class FreightInvoiceSnapshotView(
    var codeText: String,
    var name: String,
    var discount: String,
    var etaMinutes: String,
    var stopCount: String,
    var reviewerText: String,
    var summary: String,
    var startsAtText: String,
    var statusLabel: String,
)

fun toFreightInvoiceSnapshotView(item: FreightInvoiceSnapshot): FreightInvoiceSnapshotView {
    return FreightInvoiceSnapshotView(
        codeText = item.code,
        name = item.name,
        discount = "%.2f".format(item.discount),
        etaMinutes = item.etaMinutes.toString(),
        stopCount = item.stopCount.toString(),
        reviewerText = item.reviewer,
        summary = item.summary,
        startsAtText = item.startsAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toFreightInvoiceSnapshotViews(items: MutableList<FreightInvoiceSnapshot>): MutableList<FreightInvoiceSnapshotView> {
    val views = mutableListOf<FreightInvoiceSnapshotView>()
    for (item in items) {
        views.add(toFreightInvoiceSnapshotView(item))
    }
    return views
}

fun sumFreightInvoiceSnapshotView(view: FreightInvoiceSnapshotView): Double {
    val parsedDiscount = view.discount.toDouble()
    val parsedEtaMinutes = view.etaMinutes.toInt()
    val parsedStopCount = view.stopCount.toInt()
    return parsedDiscount + parsedEtaMinutes.toDouble() + parsedStopCount.toDouble()
}
