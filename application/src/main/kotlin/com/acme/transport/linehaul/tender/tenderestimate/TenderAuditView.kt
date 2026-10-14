package com.acme.transport.linehaul.tender.tenderestimate

import com.acme.transport.kit.text.truncate

/** How screens and exports show a tender audit. */
data class TenderAuditView(
    var code: String,
    var titleText: String,
    var amount: String,
    var capacity: String,
    var phoneText: String,
    var segment: String,
    var statusLabel: String,
)

fun toTenderAuditView(item: TenderAudit): TenderAuditView {
    return TenderAuditView(
        code = item.code,
        titleText = item.title,
        amount = "%.2f".format(item.amount),
        capacity = item.capacity.toString(),
        phoneText = item.phone,
        segment = truncate(item.segment, 80),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTenderAuditViews(items: MutableList<TenderAudit>): MutableList<TenderAuditView> {
    val views = mutableListOf<TenderAuditView>()
    for (item in items) {
        views.add(toTenderAuditView(item))
    }
    return views
}

fun sumTenderAuditView(view: TenderAuditView): Double {
    val parsedAmount = view.amount.toDouble()
    val parsedCapacity = view.capacity.toInt()
    return parsedAmount + parsedCapacity.toDouble()
}
