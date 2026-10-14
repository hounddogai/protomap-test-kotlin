package com.acme.transport.freightpay.audit.bundle

import com.acme.transport.kit.text.truncate

/** How screens and exports show a audit bundle. */
data class AuditBundleView(
    var code: String,
    var label: String,
    var contactEmail: String,
    var source: String,
    var checksum: String,
    var amount: String,
    var statusLabel: String,
)

fun toAuditBundleView(item: AuditBundle): AuditBundleView {
    return AuditBundleView(
        code = truncate(item.code, 64),
        label = item.label,
        contactEmail = truncate(item.contactEmail, 40),
        source = item.source,
        checksum = item.checksum,
        amount = "%.2f".format(item.amount),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAuditBundleViews(items: MutableList<AuditBundle>): MutableList<AuditBundleView> {
    val views = mutableListOf<AuditBundleView>()
    for (item in items) {
        views.add(toAuditBundleView(item))
    }
    return views
}

fun sumAuditBundleView(view: AuditBundleView): Double {
    val parsedAmount = view.amount.toDouble()
    return parsedAmount
}
