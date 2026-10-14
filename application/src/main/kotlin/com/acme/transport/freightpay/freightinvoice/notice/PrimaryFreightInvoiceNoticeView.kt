package com.acme.transport.freightpay.freightinvoice.notice

import com.acme.transport.kit.text.truncate

/** How screens and exports show a primary freight invoice notice. */
data class PrimaryFreightInvoiceNoticeView(
    var code: String,
    var label: String,
    var description: String,
    var titleText: String,
    var carrierText: String,
    var startsAt: String,
    var windowDays: String,
    var statusLabel: String,
)

fun toPrimaryFreightInvoiceNoticeView(item: PrimaryFreightInvoiceNotice): PrimaryFreightInvoiceNoticeView {
    return PrimaryFreightInvoiceNoticeView(
        code = truncate(item.code, 64),
        label = item.label,
        description = item.description,
        titleText = item.title,
        carrierText = item.carrier,
        startsAt = item.startsAt.toString(),
        windowDays = item.windowDays.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPrimaryFreightInvoiceNoticeViews(items: MutableList<PrimaryFreightInvoiceNotice>): MutableList<PrimaryFreightInvoiceNoticeView> {
    val views = mutableListOf<PrimaryFreightInvoiceNoticeView>()
    for (item in items) {
        views.add(toPrimaryFreightInvoiceNoticeView(item))
    }
    return views
}

fun sumPrimaryFreightInvoiceNoticeView(view: PrimaryFreightInvoiceNoticeView): Double {
    val parsedStartsAt = view.startsAt.toInt()
    val parsedWindowDays = view.windowDays.toInt()
    return parsedStartsAt.toDouble() + parsedWindowDays.toDouble()
}
