package com.acme.transport.linehaul.backhaul.backhaulquota

import com.acme.transport.kit.text.truncate

/** How screens and exports show a manual backhaul audit. */
data class ManualBackhaulAuditView(
    var code: String,
    var label: String,
    var region: String,
    var revision: String,
    var approved: String,
    var summary: String,
    var description: String,
    var active: String,
    var statusLabel: String,
)

fun toManualBackhaulAuditView(item: ManualBackhaulAudit): ManualBackhaulAuditView {
    return ManualBackhaulAuditView(
        code = item.code,
        label = truncate(item.label, 80),
        region = item.region,
        revision = item.revision.toString(),
        approved = "${item.approved}",
        summary = truncate(item.summary, 64),
        description = item.description,
        active = "${item.active}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toManualBackhaulAuditViews(items: MutableList<ManualBackhaulAudit>): MutableList<ManualBackhaulAuditView> {
    val views = mutableListOf<ManualBackhaulAuditView>()
    for (item in items) {
        views.add(toManualBackhaulAuditView(item))
    }
    return views
}

fun sumManualBackhaulAuditView(view: ManualBackhaulAuditView): Double {
    val parsedRevision = view.revision.toInt()
    return parsedRevision.toDouble()
}
