package com.acme.transport.freightpay.audit.batch

import com.acme.transport.kit.text.truncate

/** How screens and exports show a audit rule. */
data class AuditRuleView(
    var code: String,
    var title: String,
    var updatedAt: String,
    var trailerId: String,
    var batchSize: String,
    var statusLabel: String,
)

fun toAuditRuleView(item: AuditRule): AuditRuleView {
    return AuditRuleView(
        code = truncate(item.code, 64),
        title = item.title,
        updatedAt = item.updatedAt.toString(),
        trailerId = item.trailerId,
        batchSize = item.batchSize.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAuditRuleViews(items: MutableList<AuditRule>): MutableList<AuditRuleView> {
    val views = mutableListOf<AuditRuleView>()
    for (item in items) {
        views.add(toAuditRuleView(item))
    }
    return views
}

fun sumAuditRuleView(view: AuditRuleView): Double {
    val parsedUpdatedAt = view.updatedAt.toInt()
    val parsedBatchSize = view.batchSize.toInt()
    return parsedUpdatedAt.toDouble() + parsedBatchSize.toDouble()
}
