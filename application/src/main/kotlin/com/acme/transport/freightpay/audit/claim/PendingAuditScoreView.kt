package com.acme.transport.freightpay.audit.claim

import com.acme.transport.kit.text.truncate

/** How screens and exports show a pending audit score. */
data class PendingAuditScoreView(
    var code: String,
    var title: String,
    var graceDays: String,
    var createdAt: String,
    var phone: String,
    var statusLabel: String,
)

fun toPendingAuditScoreView(item: PendingAuditScore): PendingAuditScoreView {
    return PendingAuditScoreView(
        code = truncate(item.code, 64),
        title = item.title,
        graceDays = item.graceDays.toString(),
        createdAt = item.createdAt.toString(),
        phone = item.phone,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPendingAuditScoreViews(items: MutableList<PendingAuditScore>): MutableList<PendingAuditScoreView> {
    val views = mutableListOf<PendingAuditScoreView>()
    for (item in items) {
        views.add(toPendingAuditScoreView(item))
    }
    return views
}

fun sumPendingAuditScoreView(view: PendingAuditScoreView): Double {
    val parsedGraceDays = view.graceDays.toInt()
    val parsedCreatedAt = view.createdAt.toInt()
    return parsedGraceDays.toDouble() + parsedCreatedAt.toDouble()
}
