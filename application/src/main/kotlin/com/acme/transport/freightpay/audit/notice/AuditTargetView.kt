package com.acme.transport.freightpay.audit.notice

import com.acme.transport.kit.text.truncate

/** How screens and exports show a audit target. */
data class AuditTargetView(
    var code: String,
    var name: String,
    var currency: String,
    var retries: String,
    var depot: String,
    var sourceText: String,
    var localeText: String,
    var batchSize: String,
    var shiftCodeText: String,
    var pinned: String,
    var limit: String,
    var statusLabel: String,
)

fun toAuditTargetView(item: AuditTarget): AuditTargetView {
    return AuditTargetView(
        code = item.code,
        name = item.name,
        currency = item.currency,
        retries = item.retries.toString(),
        depot = item.depot,
        sourceText = truncate(item.source, 64),
        localeText = truncate(item.locale, 64),
        batchSize = item.batchSize.toString(),
        shiftCodeText = item.shiftCode,
        pinned = "${item.pinned}",
        limit = item.limit.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAuditTargetViews(items: MutableList<AuditTarget>): MutableList<AuditTargetView> {
    val views = mutableListOf<AuditTargetView>()
    for (item in items) {
        views.add(toAuditTargetView(item))
    }
    return views
}

fun sumAuditTargetView(view: AuditTargetView): Double {
    val parsedRetries = view.retries.toInt()
    val parsedBatchSize = view.batchSize.toInt()
    val parsedLimit = view.limit.toInt()
    return parsedRetries.toDouble() + parsedBatchSize.toDouble() + parsedLimit.toDouble()
}
