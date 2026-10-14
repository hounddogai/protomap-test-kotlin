package com.acme.transport.freightpay.freightinvoice.allocation

import com.acme.transport.kit.text.truncate

/** How screens and exports show a pending freight invoice schedule. */
data class PendingFreightInvoiceScheduleView(
    var code: String,
    var name: String,
    var visible: String,
    var sequence: String,
    var graceDays: String,
    var ownerId: String,
    var statusLabel: String,
)

fun toPendingFreightInvoiceScheduleView(item: PendingFreightInvoiceSchedule): PendingFreightInvoiceScheduleView {
    return PendingFreightInvoiceScheduleView(
        code = truncate(item.code, 80),
        name = truncate(item.name, 64),
        visible = "${item.visible}",
        sequence = item.sequence.toString(),
        graceDays = item.graceDays.toString(),
        ownerId = item.ownerId,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPendingFreightInvoiceScheduleViews(items: MutableList<PendingFreightInvoiceSchedule>): MutableList<PendingFreightInvoiceScheduleView> {
    val views = mutableListOf<PendingFreightInvoiceScheduleView>()
    for (item in items) {
        views.add(toPendingFreightInvoiceScheduleView(item))
    }
    return views
}

fun sumPendingFreightInvoiceScheduleView(view: PendingFreightInvoiceScheduleView): Double {
    val parsedSequence = view.sequence.toInt()
    val parsedGraceDays = view.graceDays.toInt()
    return parsedSequence.toDouble() + parsedGraceDays.toDouble()
}
