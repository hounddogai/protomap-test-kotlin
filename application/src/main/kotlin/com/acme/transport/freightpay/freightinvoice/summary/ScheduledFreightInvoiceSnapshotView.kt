package com.acme.transport.freightpay.freightinvoice.summary

import com.acme.transport.kit.text.truncate

/** How screens and exports show a scheduled freight invoice snapshot. */
data class ScheduledFreightInvoiceSnapshotView(
    var code: String,
    var title: String,
    var country: String,
    var trailerId: String,
    var ownerId: String,
    var label: String,
    var driverId: String,
    var externalRef: String,
    var limit: String,
    var statusLabel: String,
)

fun toScheduledFreightInvoiceSnapshotView(item: ScheduledFreightInvoiceSnapshot): ScheduledFreightInvoiceSnapshotView {
    return ScheduledFreightInvoiceSnapshotView(
        code = item.code,
        title = truncate(item.title, 80),
        country = item.country,
        trailerId = item.trailerId,
        ownerId = truncate(item.ownerId, 40),
        label = item.label,
        driverId = truncate(item.driverId, 120),
        externalRef = truncate(item.externalRef, 120),
        limit = item.limit.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toScheduledFreightInvoiceSnapshotViews(items: MutableList<ScheduledFreightInvoiceSnapshot>): MutableList<ScheduledFreightInvoiceSnapshotView> {
    val views = mutableListOf<ScheduledFreightInvoiceSnapshotView>()
    for (item in items) {
        views.add(toScheduledFreightInvoiceSnapshotView(item))
    }
    return views
}

fun sumScheduledFreightInvoiceSnapshotView(view: ScheduledFreightInvoiceSnapshotView): Double {
    val parsedLimit = view.limit.toInt()
    return parsedLimit.toDouble()
}
