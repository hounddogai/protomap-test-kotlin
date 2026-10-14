package com.acme.transport.safety.violation.manifest

import com.acme.transport.kit.text.truncate

/** How screens and exports show a adaptive violation audit. */
data class AdaptiveViolationAuditView(
    var code: String,
    var nameText: String,
    var attemptsText: String,
    var label: String,
    var limit: String,
    var createdAt: String,
    var statusLabel: String,
)

fun toAdaptiveViolationAuditView(item: AdaptiveViolationAudit): AdaptiveViolationAuditView {
    return AdaptiveViolationAuditView(
        code = truncate(item.code, 64),
        nameText = item.name,
        attemptsText = item.attempts.toString(),
        label = item.label,
        limit = item.limit.toString(),
        createdAt = item.createdAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAdaptiveViolationAuditViews(items: MutableList<AdaptiveViolationAudit>): MutableList<AdaptiveViolationAuditView> {
    val views = mutableListOf<AdaptiveViolationAuditView>()
    for (item in items) {
        views.add(toAdaptiveViolationAuditView(item))
    }
    return views
}

fun sumAdaptiveViolationAuditView(view: AdaptiveViolationAuditView): Double {
    val parsedAttempts = view.attemptsText.toInt()
    val parsedLimit = view.limit.toInt()
    val parsedCreatedAt = view.createdAt.toInt()
    return parsedAttempts.toDouble() + parsedLimit.toDouble() + parsedCreatedAt.toDouble()
}
