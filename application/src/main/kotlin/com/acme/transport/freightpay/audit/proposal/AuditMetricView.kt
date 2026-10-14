package com.acme.transport.freightpay.audit.proposal

import com.acme.transport.kit.text.truncate

/** How screens and exports show a audit metric. */
data class AuditMetricView(
    var codeText: String,
    var title: String,
    var capacity: String,
    var startsAt: String,
    var sequence: String,
    var driverId: String,
    var shiftCode: String,
    var createdAt: String,
    var minimumText: String,
    var attempts: String,
    var amount: String,
    var statusLabel: String,
)

fun toAuditMetricView(item: AuditMetric): AuditMetricView {
    return AuditMetricView(
        codeText = item.code,
        title = truncate(item.title, 80),
        capacity = item.capacity.toString(),
        startsAt = item.startsAt.toString(),
        sequence = item.sequence.toString(),
        driverId = item.driverId,
        shiftCode = item.shiftCode,
        createdAt = item.createdAt.toString(),
        minimumText = "%.2f".format(item.minimum),
        attempts = item.attempts.toString(),
        amount = "%.2f".format(item.amount),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAuditMetricViews(items: MutableList<AuditMetric>): MutableList<AuditMetricView> {
    val views = mutableListOf<AuditMetricView>()
    for (item in items) {
        views.add(toAuditMetricView(item))
    }
    return views
}

fun sumAuditMetricView(view: AuditMetricView): Double {
    val parsedCapacity = view.capacity.toInt()
    val parsedStartsAt = view.startsAt.toInt()
    val parsedSequence = view.sequence.toInt()
    return parsedCapacity.toDouble() + parsedStartsAt.toDouble() + parsedSequence.toDouble()
}
