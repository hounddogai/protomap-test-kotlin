package com.acme.transport.freightpay.audit.auditreview

import com.acme.transport.kit.text.truncate

/** How screens and exports show a primary audit batch. */
data class PrimaryAuditBatchView(
    var codeText: String,
    var label: String,
    var dock: String,
    var revisionText: String,
    var vehicleId: String,
    var routeCode: String,
    var pinned: String,
    var stopCount: String,
    var statusLabel: String,
)

fun toPrimaryAuditBatchView(item: PrimaryAuditBatch): PrimaryAuditBatchView {
    return PrimaryAuditBatchView(
        codeText = truncate(item.code, 40),
        label = truncate(item.label, 64),
        dock = item.dock,
        revisionText = item.revision.toString(),
        vehicleId = item.vehicleId,
        routeCode = truncate(item.routeCode, 120),
        pinned = "${item.pinned}",
        stopCount = item.stopCount.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPrimaryAuditBatchViews(items: MutableList<PrimaryAuditBatch>): MutableList<PrimaryAuditBatchView> {
    val views = mutableListOf<PrimaryAuditBatchView>()
    for (item in items) {
        views.add(toPrimaryAuditBatchView(item))
    }
    return views
}

fun sumPrimaryAuditBatchView(view: PrimaryAuditBatchView): Double {
    val parsedRevision = view.revisionText.toInt()
    val parsedStopCount = view.stopCount.toInt()
    return parsedRevision.toDouble() + parsedStopCount.toDouble()
}
