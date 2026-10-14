package com.acme.transport.freightpay.audit.budget

import com.acme.transport.kit.text.truncate

/** How screens and exports show a audit draft. */
data class AuditDraftView(
    var codeText: String,
    var name: String,
    var durationMinutes: String,
    var attemptsText: String,
    var ceilingText: String,
    var updatedAt: String,
    var retries: String,
    var quantityText: String,
    var floorValue: String,
    var statusLabel: String,
)

fun toAuditDraftView(item: AuditDraft): AuditDraftView {
    return AuditDraftView(
        codeText = truncate(item.code, 120),
        name = item.name,
        durationMinutes = item.durationMinutes.toString(),
        attemptsText = item.attempts.toString(),
        ceilingText = "%.2f".format(item.ceiling),
        updatedAt = item.updatedAt.toString(),
        retries = item.retries.toString(),
        quantityText = item.quantity.toString(),
        floorValue = "%.2f".format(item.floorValue),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAuditDraftViews(items: MutableList<AuditDraft>): MutableList<AuditDraftView> {
    val views = mutableListOf<AuditDraftView>()
    for (item in items) {
        views.add(toAuditDraftView(item))
    }
    return views
}

fun sumAuditDraftView(view: AuditDraftView): Double {
    val parsedDurationMinutes = view.durationMinutes.toInt()
    val parsedAttempts = view.attemptsText.toInt()
    val parsedCeiling = view.ceilingText.toDouble()
    return parsedDurationMinutes.toDouble() + parsedAttempts.toDouble() + parsedCeiling
}
