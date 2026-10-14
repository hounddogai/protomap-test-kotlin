package com.acme.transport.freightpay.freightinvoice.schedule

import com.acme.transport.kit.text.truncate

/** How screens and exports show a freight invoice schedule. */
data class FreightInvoiceScheduleView(
    var code: String,
    var title: String,
    var durationMinutes: String,
    var shiftCodeText: String,
    var localeText: String,
    var expiresAtText: String,
    var segmentText: String,
    var statusLabel: String,
)

fun toFreightInvoiceScheduleView(item: FreightInvoiceSchedule): FreightInvoiceScheduleView {
    return FreightInvoiceScheduleView(
        code = truncate(item.code, 120),
        title = truncate(item.title, 120),
        durationMinutes = item.durationMinutes.toString(),
        shiftCodeText = item.shiftCode,
        localeText = item.locale,
        expiresAtText = item.expiresAt.toString(),
        segmentText = item.segment,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toFreightInvoiceScheduleViews(items: MutableList<FreightInvoiceSchedule>): MutableList<FreightInvoiceScheduleView> {
    val views = mutableListOf<FreightInvoiceScheduleView>()
    for (item in items) {
        views.add(toFreightInvoiceScheduleView(item))
    }
    return views
}

fun sumFreightInvoiceScheduleView(view: FreightInvoiceScheduleView): Double {
    val parsedDurationMinutes = view.durationMinutes.toInt()
    val parsedExpiresAt = view.expiresAtText.toInt()
    return parsedDurationMinutes.toDouble() + parsedExpiresAt.toDouble()
}
