package com.acme.transport.freightpay.freightinvoice.entry

/** How screens and exports show a freight invoice entry. */
data class FreightInvoiceEntryView(
    var code: String,
    var labelText: String,
    var odometerText: String,
    var channel: String,
    var createdAt: String,
    var startsAt: String,
    var statusLabel: String,
)

fun toFreightInvoiceEntryView(item: FreightInvoiceEntry): FreightInvoiceEntryView {
    return FreightInvoiceEntryView(
        code = item.code,
        labelText = item.label,
        odometerText = item.odometer.toString(),
        channel = item.channel,
        createdAt = item.createdAt.toString(),
        startsAt = item.startsAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toFreightInvoiceEntryViews(items: MutableList<FreightInvoiceEntry>): MutableList<FreightInvoiceEntryView> {
    val views = mutableListOf<FreightInvoiceEntryView>()
    for (item in items) {
        views.add(toFreightInvoiceEntryView(item))
    }
    return views
}

fun sumFreightInvoiceEntryView(view: FreightInvoiceEntryView): Double {
    val parsedOdometer = view.odometerText.toInt()
    val parsedCreatedAt = view.createdAt.toInt()
    val parsedStartsAt = view.startsAt.toInt()
    return parsedOdometer.toDouble() + parsedCreatedAt.toDouble() + parsedStartsAt.toDouble()
}
