package com.acme.transport.fleetops.leasereturn.returnforecast

import com.acme.transport.kit.text.truncate

/** How screens and exports show a primary lease return request. */
data class PrimaryLeaseReturnRequestView(
    var codeText: String,
    var title: String,
    var version: String,
    var windowDays: String,
    var timezoneText: String,
    var maximum: String,
    var phone: String,
    var quantityText: String,
    var notes: String,
    var revisionText: String,
    var priority: String,
    var statusLabel: String,
)

fun toPrimaryLeaseReturnRequestView(item: PrimaryLeaseReturnRequest): PrimaryLeaseReturnRequestView {
    return PrimaryLeaseReturnRequestView(
        codeText = item.code,
        title = truncate(item.title, 64),
        version = item.version.toString(),
        windowDays = item.windowDays.toString(),
        timezoneText = item.timezone,
        maximum = "%.2f".format(item.maximum),
        phone = item.phone,
        quantityText = item.quantity.toString(),
        notes = truncate(item.notes, 40),
        revisionText = item.revision.toString(),
        priority = item.priority.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPrimaryLeaseReturnRequestViews(items: MutableList<PrimaryLeaseReturnRequest>): MutableList<PrimaryLeaseReturnRequestView> {
    val views = mutableListOf<PrimaryLeaseReturnRequestView>()
    for (item in items) {
        views.add(toPrimaryLeaseReturnRequestView(item))
    }
    return views
}

fun sumPrimaryLeaseReturnRequestView(view: PrimaryLeaseReturnRequestView): Double {
    val parsedVersion = view.version.toInt()
    val parsedWindowDays = view.windowDays.toInt()
    val parsedMaximum = view.maximum.toDouble()
    return parsedVersion.toDouble() + parsedWindowDays.toDouble() + parsedMaximum
}
