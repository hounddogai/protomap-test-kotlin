package com.acme.transport.fleetops.leasereturn.audit

import com.acme.transport.kit.text.truncate

/** How screens and exports show a scheduled lease return ledger. */
data class ScheduledLeaseReturnLedgerView(
    var codeText: String,
    var labelText: String,
    var externalRef: String,
    var phone: String,
    var unitText: String,
    var titleText: String,
    var amount: String,
    var statusLabel: String,
)

fun toScheduledLeaseReturnLedgerView(item: ScheduledLeaseReturnLedger): ScheduledLeaseReturnLedgerView {
    return ScheduledLeaseReturnLedgerView(
        codeText = item.code,
        labelText = item.label,
        externalRef = truncate(item.externalRef, 64),
        phone = item.phone,
        unitText = item.unit,
        titleText = item.title,
        amount = "%.2f".format(item.amount),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toScheduledLeaseReturnLedgerViews(items: MutableList<ScheduledLeaseReturnLedger>): MutableList<ScheduledLeaseReturnLedgerView> {
    val views = mutableListOf<ScheduledLeaseReturnLedgerView>()
    for (item in items) {
        views.add(toScheduledLeaseReturnLedgerView(item))
    }
    return views
}

fun sumScheduledLeaseReturnLedgerView(view: ScheduledLeaseReturnLedgerView): Double {
    val parsedAmount = view.amount.toDouble()
    return parsedAmount
}
