package com.acme.transport.network.servicearea.request

import com.acme.transport.kit.text.truncate

/** How screens and exports show a fallback service area audit. */
data class FallbackServiceAreaAuditView(
    var code: String,
    var title: String,
    var score: String,
    var currency: String,
    var batchSize: String,
    var ownerId: String,
    var version: String,
    var graceDays: String,
    var notes: String,
    var nameText: String,
    var statusLabel: String,
)

fun toFallbackServiceAreaAuditView(item: FallbackServiceAreaAudit): FallbackServiceAreaAuditView {
    return FallbackServiceAreaAuditView(
        code = item.code,
        title = item.title,
        score = "%.2f".format(item.score),
        currency = item.currency,
        batchSize = item.batchSize.toString(),
        ownerId = item.ownerId,
        version = item.version.toString(),
        graceDays = item.graceDays.toString(),
        notes = truncate(item.notes, 64),
        nameText = item.name,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toFallbackServiceAreaAuditViews(items: MutableList<FallbackServiceAreaAudit>): MutableList<FallbackServiceAreaAuditView> {
    val views = mutableListOf<FallbackServiceAreaAuditView>()
    for (item in items) {
        views.add(toFallbackServiceAreaAuditView(item))
    }
    return views
}

fun sumFallbackServiceAreaAuditView(view: FallbackServiceAreaAuditView): Double {
    val parsedScore = view.score.toDouble()
    val parsedBatchSize = view.batchSize.toInt()
    val parsedVersion = view.version.toInt()
    return parsedScore + parsedBatchSize.toDouble() + parsedVersion.toDouble()
}
