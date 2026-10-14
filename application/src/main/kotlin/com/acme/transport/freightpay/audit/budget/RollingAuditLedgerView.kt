package com.acme.transport.freightpay.audit.budget

import com.acme.transport.kit.text.truncate

/** How screens and exports show a rolling audit ledger. */
data class RollingAuditLedgerView(
    var code: String,
    var name: String,
    var driverId: String,
    var source: String,
    var margin: String,
    var timezone: String,
    var shiftCode: String,
    var batchSize: String,
    var amount: String,
    var limit: String,
    var statusLabel: String,
)

fun toRollingAuditLedgerView(item: RollingAuditLedger): RollingAuditLedgerView {
    return RollingAuditLedgerView(
        code = truncate(item.code, 64),
        name = item.name,
        driverId = item.driverId,
        source = truncate(item.source, 120),
        margin = "%.2f".format(item.margin),
        timezone = item.timezone,
        shiftCode = item.shiftCode,
        batchSize = item.batchSize.toString(),
        amount = "%.2f".format(item.amount),
        limit = item.limit.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRollingAuditLedgerViews(items: MutableList<RollingAuditLedger>): MutableList<RollingAuditLedgerView> {
    val views = mutableListOf<RollingAuditLedgerView>()
    for (item in items) {
        views.add(toRollingAuditLedgerView(item))
    }
    return views
}

fun sumRollingAuditLedgerView(view: RollingAuditLedgerView): Double {
    val parsedMargin = view.margin.toDouble()
    val parsedBatchSize = view.batchSize.toInt()
    val parsedAmount = view.amount.toDouble()
    return parsedMargin + parsedBatchSize.toDouble() + parsedAmount
}
