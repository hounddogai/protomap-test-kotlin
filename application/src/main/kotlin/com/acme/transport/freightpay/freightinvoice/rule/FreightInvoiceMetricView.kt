package com.acme.transport.freightpay.freightinvoice.rule

import com.acme.transport.kit.text.truncate

/** How screens and exports show a freight invoice metric. */
data class FreightInvoiceMetricView(
    var code: String,
    var nameText: String,
    var pallets: String,
    var rate: String,
    var minimumText: String,
    var contactEmail: String,
    var laneText: String,
    var statusLabel: String,
)

fun toFreightInvoiceMetricView(item: FreightInvoiceMetric): FreightInvoiceMetricView {
    return FreightInvoiceMetricView(
        code = item.code,
        nameText = truncate(item.name, 64),
        pallets = item.pallets.toString(),
        rate = "%.2f".format(item.rate),
        minimumText = "%.2f".format(item.minimum),
        contactEmail = item.contactEmail,
        laneText = item.lane,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toFreightInvoiceMetricViews(items: MutableList<FreightInvoiceMetric>): MutableList<FreightInvoiceMetricView> {
    val views = mutableListOf<FreightInvoiceMetricView>()
    for (item in items) {
        views.add(toFreightInvoiceMetricView(item))
    }
    return views
}

fun sumFreightInvoiceMetricView(view: FreightInvoiceMetricView): Double {
    val parsedPallets = view.pallets.toInt()
    val parsedRate = view.rate.toDouble()
    val parsedMinimum = view.minimumText.toDouble()
    return parsedPallets.toDouble() + parsedRate + parsedMinimum
}
