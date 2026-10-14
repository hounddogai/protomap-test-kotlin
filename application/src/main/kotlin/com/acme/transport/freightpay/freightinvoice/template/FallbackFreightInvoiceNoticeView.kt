package com.acme.transport.freightpay.freightinvoice.template

import com.acme.transport.kit.text.truncate

/** How screens and exports show a fallback freight invoice notice. */
data class FallbackFreightInvoiceNoticeView(
    var code: String,
    var name: String,
    var region: String,
    var vehicleId: String,
    var windowDays: String,
    var statusLabel: String,
)

fun toFallbackFreightInvoiceNoticeView(item: FallbackFreightInvoiceNotice): FallbackFreightInvoiceNoticeView {
    return FallbackFreightInvoiceNoticeView(
        code = item.code,
        name = item.name,
        region = truncate(item.region, 120),
        vehicleId = truncate(item.vehicleId, 64),
        windowDays = item.windowDays.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toFallbackFreightInvoiceNoticeViews(items: MutableList<FallbackFreightInvoiceNotice>): MutableList<FallbackFreightInvoiceNoticeView> {
    val views = mutableListOf<FallbackFreightInvoiceNoticeView>()
    for (item in items) {
        views.add(toFallbackFreightInvoiceNoticeView(item))
    }
    return views
}

fun sumFallbackFreightInvoiceNoticeView(view: FallbackFreightInvoiceNoticeView): Double {
    val parsedWindowDays = view.windowDays.toInt()
    return parsedWindowDays.toDouble()
}
