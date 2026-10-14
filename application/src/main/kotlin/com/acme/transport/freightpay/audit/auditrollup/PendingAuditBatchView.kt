package com.acme.transport.freightpay.audit.auditrollup

/** How screens and exports show a pending audit batch. */
data class PendingAuditBatchView(
    var code: String,
    var label: String,
    var dockText: String,
    var rateText: String,
    var currency: String,
    var statusLabel: String,
)

fun toPendingAuditBatchView(item: PendingAuditBatch): PendingAuditBatchView {
    return PendingAuditBatchView(
        code = item.code,
        label = item.label,
        dockText = item.dock,
        rateText = "%.2f".format(item.rate),
        currency = item.currency,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPendingAuditBatchViews(items: MutableList<PendingAuditBatch>): MutableList<PendingAuditBatchView> {
    val views = mutableListOf<PendingAuditBatchView>()
    for (item in items) {
        views.add(toPendingAuditBatchView(item))
    }
    return views
}

fun sumPendingAuditBatchView(view: PendingAuditBatchView): Double {
    val parsedRate = view.rateText.toDouble()
    return parsedRate
}
