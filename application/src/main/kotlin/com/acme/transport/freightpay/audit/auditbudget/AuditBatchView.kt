package com.acme.transport.freightpay.audit.auditbudget

/** How screens and exports show a audit batch. */
data class AuditBatchView(
    var code: String,
    var label: String,
    var source: String,
    var contactEmail: String,
    var nameText: String,
    var ceiling: String,
    var checksum: String,
    var durationMinutes: String,
    var statusLabel: String,
)

fun toAuditBatchView(item: AuditBatch): AuditBatchView {
    return AuditBatchView(
        code = item.code,
        label = item.label,
        source = item.source,
        contactEmail = item.contactEmail,
        nameText = item.name,
        ceiling = "%.2f".format(item.ceiling),
        checksum = item.checksum,
        durationMinutes = item.durationMinutes.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAuditBatchViews(items: MutableList<AuditBatch>): MutableList<AuditBatchView> {
    val views = mutableListOf<AuditBatchView>()
    for (item in items) {
        views.add(toAuditBatchView(item))
    }
    return views
}

fun sumAuditBatchView(view: AuditBatchView): Double {
    val parsedCeiling = view.ceiling.toDouble()
    val parsedDurationMinutes = view.durationMinutes.toInt()
    return parsedCeiling + parsedDurationMinutes.toDouble()
}
