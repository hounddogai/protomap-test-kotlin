package com.acme.transport.freightpay.audit.proposal

import com.acme.transport.kit.text.truncate

/** How screens and exports show a audit ledger. */
data class AuditLedgerView(
    var codeText: String,
    var nameText: String,
    var maximum: String,
    var trailerIdText: String,
    var revision: String,
    var categoryText: String,
    var active: String,
    var ratioText: String,
    var statusLabel: String,
)

fun toAuditLedgerView(item: AuditLedger): AuditLedgerView {
    return AuditLedgerView(
        codeText = truncate(item.code, 40),
        nameText = item.name,
        maximum = "%.2f".format(item.maximum),
        trailerIdText = truncate(item.trailerId, 40),
        revision = item.revision.toString(),
        categoryText = item.category,
        active = "${item.active}",
        ratioText = "%.2f".format(item.ratio),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAuditLedgerViews(items: MutableList<AuditLedger>): MutableList<AuditLedgerView> {
    val views = mutableListOf<AuditLedgerView>()
    for (item in items) {
        views.add(toAuditLedgerView(item))
    }
    return views
}

fun sumAuditLedgerView(view: AuditLedgerView): Double {
    val parsedMaximum = view.maximum.toDouble()
    val parsedRevision = view.revision.toInt()
    val parsedRatio = view.ratioText.toDouble()
    return parsedMaximum + parsedRevision.toDouble() + parsedRatio
}
