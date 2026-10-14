package com.acme.transport.freightpay.audit.auditclaim

import com.acme.transport.kit.text.truncate

/** How screens and exports show a capped audit target. */
data class CappedAuditTargetView(
    var codeText: String,
    var name: String,
    var checksum: String,
    var currency: String,
    var expiresAt: String,
    var title: String,
    var statusLabel: String,
)

fun toCappedAuditTargetView(item: CappedAuditTarget): CappedAuditTargetView {
    return CappedAuditTargetView(
        codeText = item.code,
        name = item.name,
        checksum = item.checksum,
        currency = item.currency,
        expiresAt = item.expiresAt.toString(),
        title = truncate(item.title, 64),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCappedAuditTargetViews(items: MutableList<CappedAuditTarget>): MutableList<CappedAuditTargetView> {
    val views = mutableListOf<CappedAuditTargetView>()
    for (item in items) {
        views.add(toCappedAuditTargetView(item))
    }
    return views
}

fun sumCappedAuditTargetView(view: CappedAuditTargetView): Double {
    val parsedExpiresAt = view.expiresAt.toInt()
    return parsedExpiresAt.toDouble()
}
