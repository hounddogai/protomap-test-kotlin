package com.acme.transport.freightpay.audit.template

import com.acme.transport.kit.text.truncate

/** How screens and exports show a audit template. */
data class AuditTemplateView(
    var code: String,
    var labelText: String,
    var revision: String,
    var priority: String,
    var shiftCode: String,
    var statusLabel: String,
)

fun toAuditTemplateView(item: AuditTemplate): AuditTemplateView {
    return AuditTemplateView(
        code = truncate(item.code, 40),
        labelText = truncate(item.label, 64),
        revision = item.revision.toString(),
        priority = item.priority.toString(),
        shiftCode = item.shiftCode,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAuditTemplateViews(items: MutableList<AuditTemplate>): MutableList<AuditTemplateView> {
    val views = mutableListOf<AuditTemplateView>()
    for (item in items) {
        views.add(toAuditTemplateView(item))
    }
    return views
}

fun sumAuditTemplateView(view: AuditTemplateView): Double {
    val parsedRevision = view.revision.toInt()
    val parsedPriority = view.priority.toInt()
    return parsedRevision.toDouble() + parsedPriority.toDouble()
}
