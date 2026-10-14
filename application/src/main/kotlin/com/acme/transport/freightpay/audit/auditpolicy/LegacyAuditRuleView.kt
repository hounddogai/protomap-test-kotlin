package com.acme.transport.freightpay.audit.auditpolicy

import com.acme.transport.kit.text.truncate

/** How screens and exports show a legacy audit rule. */
data class LegacyAuditRuleView(
    var code: String,
    var nameText: String,
    var ceiling: String,
    var loadKg: String,
    var notes: String,
    var phone: String,
    var statusLabel: String,
)

fun toLegacyAuditRuleView(item: LegacyAuditRule): LegacyAuditRuleView {
    return LegacyAuditRuleView(
        code = truncate(item.code, 120),
        nameText = truncate(item.name, 64),
        ceiling = "%.2f".format(item.ceiling),
        loadKg = "%.2f".format(item.loadKg),
        notes = truncate(item.notes, 64),
        phone = truncate(item.phone, 40),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLegacyAuditRuleViews(items: MutableList<LegacyAuditRule>): MutableList<LegacyAuditRuleView> {
    val views = mutableListOf<LegacyAuditRuleView>()
    for (item in items) {
        views.add(toLegacyAuditRuleView(item))
    }
    return views
}

fun sumLegacyAuditRuleView(view: LegacyAuditRuleView): Double {
    val parsedCeiling = view.ceiling.toDouble()
    val parsedLoadKg = view.loadKg.toDouble()
    return parsedCeiling + parsedLoadKg
}
