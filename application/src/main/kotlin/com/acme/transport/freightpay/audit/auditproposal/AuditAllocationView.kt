package com.acme.transport.freightpay.audit.auditproposal

import com.acme.transport.kit.text.truncate

/** How screens and exports show a audit allocation. */
data class AuditAllocationView(
    var codeText: String,
    var title: String,
    var currency: String,
    var locale: String,
    var graceDays: String,
    var priority: String,
    var stopCount: String,
    var endsAt: String,
    var ratio: String,
    var statusLabel: String,
)

fun toAuditAllocationView(item: AuditAllocation): AuditAllocationView {
    return AuditAllocationView(
        codeText = truncate(item.code, 120),
        title = item.title,
        currency = item.currency,
        locale = item.locale,
        graceDays = item.graceDays.toString(),
        priority = item.priority.toString(),
        stopCount = item.stopCount.toString(),
        endsAt = item.endsAt.toString(),
        ratio = "%.2f".format(item.ratio),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAuditAllocationViews(items: MutableList<AuditAllocation>): MutableList<AuditAllocationView> {
    val views = mutableListOf<AuditAllocationView>()
    for (item in items) {
        views.add(toAuditAllocationView(item))
    }
    return views
}

fun sumAuditAllocationView(view: AuditAllocationView): Double {
    val parsedGraceDays = view.graceDays.toInt()
    val parsedPriority = view.priority.toInt()
    val parsedStopCount = view.stopCount.toInt()
    return parsedGraceDays.toDouble() + parsedPriority.toDouble() + parsedStopCount.toDouble()
}
