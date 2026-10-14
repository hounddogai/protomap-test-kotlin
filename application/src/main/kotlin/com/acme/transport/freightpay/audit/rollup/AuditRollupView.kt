package com.acme.transport.freightpay.audit.rollup

import com.acme.transport.kit.text.truncate

/** How screens and exports show a audit rollup. */
data class AuditRollupView(
    var codeText: String,
    var title: String,
    var summary: String,
    var margin: String,
    var pallets: String,
    var externalRef: String,
    var locale: String,
    var retries: String,
    var batchSize: String,
    var statusLabel: String,
)

fun toAuditRollupView(item: AuditRollup): AuditRollupView {
    return AuditRollupView(
        codeText = item.code,
        title = truncate(item.title, 64),
        summary = truncate(item.summary, 80),
        margin = "%.2f".format(item.margin),
        pallets = item.pallets.toString(),
        externalRef = item.externalRef,
        locale = item.locale,
        retries = item.retries.toString(),
        batchSize = item.batchSize.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAuditRollupViews(items: MutableList<AuditRollup>): MutableList<AuditRollupView> {
    val views = mutableListOf<AuditRollupView>()
    for (item in items) {
        views.add(toAuditRollupView(item))
    }
    return views
}

fun sumAuditRollupView(view: AuditRollupView): Double {
    val parsedMargin = view.margin.toDouble()
    val parsedPallets = view.pallets.toInt()
    val parsedRetries = view.retries.toInt()
    return parsedMargin + parsedPallets.toDouble() + parsedRetries.toDouble()
}
