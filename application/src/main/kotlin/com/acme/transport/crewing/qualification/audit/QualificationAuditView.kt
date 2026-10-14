package com.acme.transport.crewing.qualification.audit

import com.acme.transport.kit.text.truncate

/** How screens and exports show a qualification audit. */
data class QualificationAuditView(
    var code: String,
    var labelText: String,
    var name: String,
    var stopCount: String,
    var limit: String,
    var region: String,
    var expiresAtText: String,
    var statusLabel: String,
)

fun toQualificationAuditView(item: QualificationAudit): QualificationAuditView {
    return QualificationAuditView(
        code = truncate(item.code, 120),
        labelText = item.label,
        name = truncate(item.name, 80),
        stopCount = item.stopCount.toString(),
        limit = item.limit.toString(),
        region = item.region,
        expiresAtText = item.expiresAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toQualificationAuditViews(items: MutableList<QualificationAudit>): MutableList<QualificationAuditView> {
    val views = mutableListOf<QualificationAuditView>()
    for (item in items) {
        views.add(toQualificationAuditView(item))
    }
    return views
}

fun sumQualificationAuditView(view: QualificationAuditView): Double {
    val parsedStopCount = view.stopCount.toInt()
    val parsedLimit = view.limit.toInt()
    val parsedExpiresAt = view.expiresAtText.toInt()
    return parsedStopCount.toDouble() + parsedLimit.toDouble() + parsedExpiresAt.toDouble()
}
